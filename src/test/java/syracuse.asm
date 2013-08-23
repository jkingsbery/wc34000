

second: equ #2

move #10,-(SP)	;2
jsr syracuse	;4
outnum d0		;6
halt			;7

syracuse:
	link a6,#-1	;8
	move second(a6),d1 ;10
	outnum d1 ;11 ; for debugging
	cmp d1,#1 ;12
	beq basecase ;14
	and #1,d1 ;16
	cmp d1,#0 ;18
	beq evencase ;20
	jmp oddcase ;22
	basecase:
		move #1,d0 ;24
		jmp endsyracuse ;26
	evencase:
		; get second/2
		move second(a6),d1 ;28
		asr #1,d1 ;30
		;call syracuse recursively
		move d1,-(SP) ;31
		jsr syracuse ;32
		;since return result is already in d0, just add 1
		add #1,d0 ;34
		jmp endsyracuse ;36
	oddcase:
		move second(a6),d1 ;38
		muls #3,d1
		add #1,d1
		move d1,-(SP)
		jsr syracuse
		add #1,d0
	endsyracuse:
		unlk a6
		rtd 2
		
