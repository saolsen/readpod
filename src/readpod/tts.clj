(ns readpod.tts
  (:import
   (com.sun.speech.freetts Voice)
   (com.sun.speech.freetts VoiceManager)
   (com.sun.speech.freetts.audio JavaClipAudioPlayer)
   (com.sun.speech.freetts.audio SingleFileAudioPlayer)
   (javax.sound.sampled AudioFileFormat$Type)))
;; In a production system this should probably run as some kind of
;; background worker process so that it can be scaled seperately from
;; the api. Consuming from a Queue of some sort. Done in process for
;; this prototype so I could run it on heroku.

;; Set system property so voicemanager can find voices.
(System/setProperty
 "freetts.voices" "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")

;; VoiceManager for creating the Voices
(def voice-manager (. VoiceManager getInstance))

;; To create a voice, allocate it and give it an AudioPlayer,
;; Currently we're saving it to the filename passed in. You'll
;; want something uniqueish so you can get the file later.

;; Returns the filename.
(defn render
  "Renders Speech to Text"
  [text filename]
  (let [audioplayer (new SingleFileAudioPlayer (str "./" filename)
                         AudioFileFormat$Type/WAVE)]
    (doto (. voice-manager getVoice "kevin16")
      (.allocate)
      (.setAudioPlayer audioplayer)
      (.speak text)
      (.deallocate))
    (.close audioplayer)
    filename))

;; There is also lots of work that can be done here as far as the voice
;; quaility is concerned. It's possible to import the CMU Arctic voices
;; from festival which sound a lot nicer than this built-in kevin16 voice.
