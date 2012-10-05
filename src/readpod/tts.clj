(ns readpod.tts
  ;(:import
   ;(com.sun.speech.freetts Voice)
   ;(com.sun.speech.freetts VoiceManager)
   ;(com.sun.speech.freetts.audio JavaClipAudioPlayer)
   ;(com.sun.speech.freetts.audio SingleFileAudioPlayer)
   ;(javax.sound.sampled AudioFileFormat$Type)))
  )
;; This should probably be run as a background worker instead of
;; in the api itself and a real system would want to store rendered
;; articles somewhere to avoid re-rendering the same file multiple times.

;; Using a java library called FreeTTS
;; Set system property so voicemanager can find voices.
;(System/setProperty
; "freetts.voices" "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory")

;; VoiceManager for creating the Voices
;(def voice-manager (. VoiceManager getInstance))

;; To create a voice, allocate it and give it an AudioPlayer,
;; Currently we're saving it to the filename passed in.

;; Returns the filename.
;(defn render
  "Renders text to speech."
;  [text filename]
;  (let [audioplayer (new SingleFileAudioPlayer (str "./" filename)
;                         AudioFileFormat$Type/WAVE)]
;    (doto (. voice-manager getVoice "kevin16")
;      (.allocate)
;      (.setAudioPlayer audioplayer)
;      (.speak text)
;      (.deallocate))
;    (.close audioplayer)
;    filename))

;; I'm using the default freetts voice right now which isn't the
;; greatest. There are other voices that can be used that sound better.

;; Going to switch to using festival for this. This means that the
;; clojure worker will communicate with an external festival
;; server. Because it's tricky to set up festival on osx I'll also
;; hopefully have a local version that just uses osx's text to speech
;; capabilities.

;(defprotocol TTSEngine
;  "Text to speech processing engine"
;  (start-up [this]
;    "Any initialization that has to be done before it can be used.")
;  (shut-down [this]
;    "Any deallocation or file cleanup that should be done")
;  (render [this text]
;    "Renders the text to audio, stores it on s3 and returns the url that
;     can be used to hit it."))

;; A TTS implementation that talks to an external Festival TTS Server
;(deftype FestivalTTS [server]
;  TTSEngine
;  (start-up [this] nil)
;  (shut-down [this] nil)
                                        ;  (render [this text] nil))

(defn render [text filename] (println "rendering"))