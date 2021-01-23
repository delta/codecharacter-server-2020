mkdir -p /home/code/Server
mkdir -p /home/code/.m2
git clone https://github.com/delta/codecharacter-storage-2021.git storage
mv storage/storage /home/code/Server/storage
rm -rf storage