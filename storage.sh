mkdir -p /home/code/Server
mkdir -p /home/code/.m2
git clone https://github.com/ElzaCS/codecharacter-storage.git storage
mv storage/storage /home/code/Server/storage
rm -rf storage