package delta.codecharacter.server.util;

import delta.codecharacter.server.controller.response.PrivateCommitResponse;
import lombok.*;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GitRepository {
    private Repository repository;
    private Git git;
    private ObjectId head;
    private Ref master;

    private final Logger Log = Logger.getLogger(GitRepository.class.getName());

    public GitRepository(Git git) {
        this.git = git;
        this.repository = git.getRepository();
        try {
            head = repository.resolve("HEAD");
            master = repository.getRef("refs/heads/master");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add() throws GitAPIException {
        AddCommand addCommand = git.add();
        addCommand.addFilepattern(".").call();
    }

    public String commit(String message) throws GitAPIException {
        CommitCommand commitCommand = git.commit();
        RevCommit commit = commitCommand.setMessage(message).call();
        return commit.getName();

    }

    public List<PrivateCommitResponse> log() throws GitAPIException {
        Iterable<RevCommit> revCommits = git.log().call();
        List<PrivateCommitResponse> commitResponses = new ArrayList<>();
        for (RevCommit commit : revCommits) {
            Date date = new Date(commit.getCommitTime() * 1000L);
            Instant instant = Instant.ofEpochMilli(date.getTime());
            LocalDateTime committedAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            PrivateCommitResponse commitResponse = PrivateCommitResponse.builder()
                    .commitHash(commit.getName())
                    .commitMessage(commit.getFullMessage())
                    .committedAt(committedAt)
                    .build();
            commitResponses.add(commitResponse);
        }
        return commitResponses;
    }

    public String getContent(String commitHash, File file) throws IOException, GitAPIException {
        ObjectId commitId = ObjectId.fromString(commitHash);
        Log.info("Commit ID: " + commitId);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(commitId);
            Log.info("Commit: " + commit + "\nCommit message: " + commit.getFullMessage());
            git.checkout().setName(commitHash).call();
            String code = GitHandler.readFile(file);
            String masterName = master.getName();
            Log.info("Master Name: " + masterName);
            git.checkout().setName(master.getName()).call();
            return code;
        }
    }

    private String getContent(RevCommit commit, String path) throws IOException {
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            Log.info("Tree walk: " + treeWalk);

            ObjectId blobId = treeWalk.getObjectId(0);
            try (ObjectReader objectReader = repository.newObjectReader()) {
                ObjectLoader objectLoader = objectReader.open(blobId);
                byte[] bytes = objectLoader.getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }
}