package delta.codecharacter.server.utils;

import lombok.*;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GitRepository {
    private Repository repository;
    private Git git;
    private ObjectId head;

    public GitRepository(Git git) {
        this.git = git;
        this.repository = git.getRepository();
        try {
            head = repository.resolve("HEAD");
        } catch (Exception e) {

        }
    }

    public boolean add() {
        AddCommand addCommand = git.add();
        try {
            addCommand.addFilepattern(".").call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String commit(String message) {
        CommitCommand commitCommand = git.commit();
        try {
            RevCommit commit = commitCommand.setMessage(message).call();
            return commit.getName();
        } catch (Exception e) {
            return null;
        }
    }
}
