#!/usr/bin/env bash
# Questlog 60-second demo video builder
# Usage: drop screenshots into ../screenshots/ (01-library.png, 02-scan.png, 03-verified.png, 04-clockin.png)
# then run: ./make-demo-video.sh   →   ../questlog-demo.mp4
set -e
cd "$(dirname "$0")/.."
SC=screenshots
OUT=questlog-demo.mp4
FONT=/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf
W=1920; H=1080
BG="#FFF8F0"; INK="#3E2723"; ACC="#E07856"

card() { # $1=text $2=seconds $3=out
  ffmpeg -y -loglevel error -f lavfi -i "color=c=$BG:s=${W}x${H}:d=$2" \
    -vf "drawtext=fontfile=$FONT:text='$1':fontcolor=$INK:fontsize=64:x=(w-text_w)/2:y=(h-text_h)/2:line_spacing=18" \
    -c:v libx264 -pix_fmt yuv420p -r 30 "$3"
}

shot() { # $1=image $2=caption $3=seconds $4=out
  local src="$1"
  if [ ! -f "$src" ]; then card "$2" "$3" "$4"; return; fi
  ffmpeg -y -loglevel error -loop 1 -i "$src" \
    -vf "scale=$W:$H:force_original_aspect_ratio=decrease,pad=$W:$H:(ow-iw)/2:(oh-ih)/2:color=$BG,zoompan=z='min(zoom+0.0008,1.12)':d=$3*30:s=${W}x${H}:fps=30,drawbox=y=ih-190:w=iw:h=190:color=0x3E2723@0.82:t=fill,drawtext=fontfile=$FONT:text='$2':fontcolor=white:fontsize=52:x=(w-text_w)/2:y=h-125" \
    -t "$3" -c:v libx264 -pix_fmt yuv420p -r 30 "$4"
}

echo "▶ building segments..."
card "Gamers discover games on TikTok —\nand forget them.\n\nQuestlog saves them. And proves them." 5 /tmp/v1.mp4
shot "$SC/02-scan.png"   "Screenshot it. Share it. Found in ~2 seconds." 10 /tmp/v2.mp4
shot "$SC/01-library.png" "Your library. Free forever. No wallet needed." 10 /tmp/v3.mp4
shot "$SC/03-verified.png" "One tap. Phantom signs. Permanent on Solana." 15 /tmp/v4.mp4
shot "$SC/04-clockin.png" "Clock in daily. Warm. Flamekeeper. Legend." 15 /tmp/v5.mp4
card "Questlog.\nYour verified gaming history." 5 /tmp/v6.mp4

echo "▶ stitching 60s demo..."
printf "file '/tmp/v1.mp4'\nfile '/tmp/v2.mp4'\nfile '/tmp/v3.mp4'\nfile '/tmp/v4.mp4'\nfile '/tmp/v5.mp4'\nfile '/tmp/v6.mp4'\n" > /tmp/vlist.txt
ffmpeg -y -loglevel error -f concat -safe 0 -i /tmp/vlist.txt -c:v libx264 -pix_fmt yuv420p -r 30 "$OUT"
echo "✅ done: $OUT ($(du -h $OUT | cut -f1))"
