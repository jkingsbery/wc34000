; Evaluates a line

mparam:	equ #2
bparam: equ #3
xval:	equ #4

move #5,-(SP)
move #1,-(SP)
move #3,-(SP)
jsr line
outnum d0
halt

line:
	link a6,#-1
	move bparam(a6),d0
	move mparam(a6),d1
	muls xval(a6),d1
	add d1,d0
	unlk a6
	rtd 2
	
