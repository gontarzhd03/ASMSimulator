#a9n8 by Hank Gontarz
LOOP1:	NOP
	CALL DISP
	CALL CHECK
	JMP LOOP1
ONE:	DB  01
#############################
DISP:	PUSH A
	PUSH SR
	LOD A, [ALPHA]
	ADD A, [ONE]
	STO A, [ALPHA]
	OUT 04, A
	POP SR
	POP A
	RET
ALPHA:	DB  20
#############################
CHECK:	PUSH A
	PUSH SR
	LOD A, [CHARS]
	SUB A, [ONE]
	JZ  NEWLN
	STO A, [CHARS]
	LOD A, [ALPHA]
	SUB A, [END]
	JZ  DOMOD
	POP SR
	POP A
	RET
DOMOD:	LOD A, [FF]
	STO A, [LOOP1]
	POP SR
	POP A
	RET
#############################
NEWLN:	LOD A, [CR]
	OUT 04, A
	LOD A, [LF]
	OUT 04, A
	LOD A, [CHARR]
	STO A, [CHARS]
	POP SR
	POP A
	RET
CHARS:	DB  1E
CHARR:	DB  1E
CR:	DB  0D
LF:	DB  0A
FF:	DB  FF
END:	DB  7E

