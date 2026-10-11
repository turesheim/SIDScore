// Embedding example for examples/sfx/effects.sidscore.
// The host installs its own IRQ, calls start once, and on_frame each video frame.
*=$3000 "HOST"

start:
  lda #1
  jsr Score.init
  rts

on_frame:
  jsr Score.play
  rts

// Call when voice 3 is free, or use a STEAL effect to preempt a lower priority effect.
trigger_noise:
  jsr Score.tune2.effect_NoiseZap
  rts

#import "score.asm"
