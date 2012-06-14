(ns readpod.tts
  (:import
   (com.sun.speech.freetts Voice)
   (com.sun.speech.freetts VoiceManager)
   (com.sun.speech.freetts.audio JavaClipAudioPlayer)
   (com.sun.speech.freetts.audio SingleFileAudioPlayer)
   (javax.sound.sampled AudioFileFormat$Type)))
;; This should probably be run as a background worker instead of
;; in the api itself and a real system would want to store rendered
;; articles somewhere to avoid re-rendering the same file multiple times.

;; Using a java library called FreeTTS
;; Set system property so voicemanager can find voices.
(System/setProperty
 "freetts.voices" "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")

;; VoiceManager for creating the Voices
(def voice-manager (. VoiceManager getInstance))

;; To create a voice, allocate it and give it an AudioPlayer,
;; Currently we're saving it to the filename passed in.

;; Returns the filename.
(defn render
  "Renders text to speech."
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

;; I'm using the default freetts voice right now which isn't the
;; greatest. There are other voices that can be used that sound better.
