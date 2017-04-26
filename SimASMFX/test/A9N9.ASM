#a9n9 by Hank Gontarz
LOOP1:	NOP
	CALL DISP
	CALL CHECK
	JMP LOOP1
ONE:	DB  01
#############################
DISP:	PUSH B
	PUSH SR
	LOD B, [ALPHA]
	ADD B, [ONE]
	STO B, [ALPHA]
	OUT 04, B
	POP SR
	POP B
	RET
ALPHA:	DB  20
#############################
CHECK:	PUSH B
	PUSH SR
	LOD B, [CHARS]
	SUB B, [ONE]
	JZ  NEWLN
	STO B, [CHARS]
	LOD B, [ALPHA]
	SUB B, [END]
	JZ  DOMOD
	POP SR
	POP B
	RET
DOMOD:	LOD B, [FF]
	STO B, [LOOP1]
	POP SR
	POP B
	RET
#############################
NEWLN:	LOD B, [CR]
	OUT 04, B
	LOD B, [LF]
	OUT 04, B
	LOD B, [CHARR]
	STO B, [CHARS]
	POP SR
	POP B
	RET
CHARS:	DB  1E
CHARR:	DB  1E
CR:	DB  0D
LF:	DB  0A
FF:	DB  FF
END:	DB  7E

