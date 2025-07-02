 #!/bin/sh
 # Shellskript zur sicherung des codes aus diesem Programm
 #
echo $(date "+%d.%m.%y - %H:%M") -- starte backup fuer dieses Projekt
SOURCE="/home/rene/fastDrive"
TARGET="/home/rene/sicherungsSpeicher"	
tar -czf $TARGET/rene/PictureDatabase_$(date "+%y%m%d-%H%M").tar \
nbproject/* \
skripte/* \
src/* \
*.xml *.sh *.html *.txt
if test $? -ne 0
then
  echo $(date "+%d.%m.%y - %H:%M") -- ERROR - tar reports error
else
  echo $(date "+%d.%m.%y - %H:%M") -- Backup finished
fi

