(ns examples.core
    (:use [overtone.live]
          [overtone.inst.sampled-piano]))

; model a plucked string. this is really cool! 
(definst plucked-string [note 60 amp 0.8 dur 2 decay 30 coef 0.3 gate 1]
  (let [freq (midicps note)
        noize (* 0.8 (white-noise))
        dly   (/ 1.0 freq)
        plk   (pluck noize gate dly dly decay coef)
        dist  (distort plk)
        filt  (rlpf dist (* 12 freq) 0.6)
        clp   (clip2 filt 0.8)
        reverb (free-verb clp 0.4 0.8 0.2)]
    (* amp (env-gen (perc 0.0001 dur) :action 0) reverb)))

(def snare (sample (freesound-path 26903)))
(def kick (sample (freesound-path 2086)))
(def ch (sample (freesound-path 802)))
(def oh (sample (freesound-path 26657)))

; define a metronome that will fire every eighth note
; at 100 bpm
(def met (metronome (* 100 2)))

(defn subdivide 
    "subdivide two time intervals by 4, and return the time interval
    at position. this is a cheap hack to schedule 16th notes without
    defining the whole pattern with the metronome firing every 16th note."
    [a b position] 
    (+ a (* position (/ (- b a) 4) )))

(defn licking-stick-drums [nome]        
    (let [beat (nome)]
        ; hi-hat pattern
        (at (nome beat) (ch))
        (at (nome (+ 1 beat)) (oh))
        (at (nome (+ 2 beat)) (ch))
        (at (nome (+ 3 beat)) (ch))
        (at (nome (+ 4 beat)) (ch))
        (at (nome (+ 5 beat)) (oh))
        (at (nome (+ 6 beat)) (ch))
        (at (nome (+ 7 beat)) (ch))

        ; snare pattern
        (at (nome (+ 2 beat)) (snare))
        (at (subdivide (nome (+ 2 beat)) (nome (+ 4 beat)) 3) (snare))
        (at (subdivide (nome (+ 4 beat)) (nome (+ 6 beat)) 1) (snare))
        (at (nome (+ 6 beat)) (snare))
        (at (subdivide (nome (+ 6 beat)) (nome (+ 8 beat)) 3) (snare))

        ; kick drum pattern
        (at (nome beat) (kick))
        (at (nome (+ 5 beat)) (kick))
        (at (nome (+ 7 beat)) (kick))
        (apply-at (nome (+ 8 beat)) licking-stick-drums nome [])))
