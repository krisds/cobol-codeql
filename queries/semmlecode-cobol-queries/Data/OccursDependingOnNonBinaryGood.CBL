000100 01  ARGUMENTS.
000200     05  ENDING-NUMBER      PIC 9(4) USAGE COMP-4.
000300 
000400 01  RETURN-VALUE.
000500     05  RETURN-LINE OCCURS 1 TO 10000 
000600                            DEPENDING ON ENDING-NUMBER 
000700                            PIC X(60).