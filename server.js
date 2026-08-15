const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const app = express();
const upload = multer({ dest: 'uploads/' });

app.post('/dub', upload.single('video'), async (req, res) => {
  try {
    const { targetLanguage, script } = req.body;
    const videoPath = req.file.path;
    const outputPath = `outputs/dubbed_${Date.now()}.mp4`;

    if (!fs.existsSync('outputs')) fs.mkdirSync('outputs');

    const audioPath = `temp_audio_${Date.now()}.wav`;
    exec(`ffmpeg -i "${videoPath}" -q:a 9 -n "${audioPath}"`, (err) => {
      if (err) return res.status(500).json({ error: err.message });
      
      const ttsPath = `temp_tts_${Date.now()}.mp3`;
      exec(`echo "Hello" | ffmpeg -f lavfi -i anullsrc=r=44100:cl=stereo -t 5 -q:a 9 "${ttsPath}"`, (err) => {
        if (err) return res.status(500).json({ error: err.message });
        
        const mixedAudioPath = `temp_mixed_${Date.now()}.wav`;
        exec(`ffmpeg -i "${audioPath}" -i "${ttsPath}" -filter_complex "[0]volume=0.3[a];[a][1]amix=inputs=2:duration=first[out]" -map "[out]" -n "${mixedAudioPath}"`, (err) => {
          if (err) return res.status(500).json({ error: err.message });
          
          exec(`ffmpeg -i "${videoPath}" -i "${mixedAudioPath}" -c:v copy -map 0:v:0 -map 1:a:0 -shortest -n "${outputPath}"`, (err) => {
            if (err) return res.status(500).json({ error: err.message });
            
            res.download(outputPath);
            
            setTimeout(() => {
              [videoPath, audioPath, ttsPath, mixedAudioPath].forEach(f => {
                if (fs.existsSync(f)) fs.unlinkSync(f);
              });
            }, 1000);
          });
        });
      });
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.listen(3000, () => {
  console.log('Dubbing server running on port 3000');
});