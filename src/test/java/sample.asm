
; Immediate
outnum #3 

; Data Register Direct
move #4,d0 
outnum d0

;Address Register Direct
move #8,a0
outnum a0

;Address Register Indirect
move #65000,a0
move #1,(a0)
outnum (a0)

;Absolute
move #314,42000
outnum 42000

halt