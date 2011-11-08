% Background model 

has_tfbs(Gene,TFBS) :- tfbs(TFBS,Gene,_,_,_,_). 

before(TF1, TF2, Gene) :- 
	 tfbs(TF1, Gene, Pos1,_,_,_), tfbs(TF2, Gene, Pos2,_,_,_), Pos1<Pos2, not(TF1 == TF2).

distance(BS1, BS2, Seq, D) :-
	 tfbs(BS1, Seq, StartPos1,EndPos1,_,_), tfbs(BS2, Seq, StartPos2, _,_,_), BS1 \== BS2, 
	 StartPos1 < StartPos2, !,
	 D is StartPos2 - EndPos1.

distance(BS1, BS2, Seq, D) :-
	 tfbs(BS1, Seq, StartPos1,_,_,_), tfbs(BS2, Seq, _, EndPos2,_,_), BS1 \== BS2,
	 D is StartPos1 - EndPos2.

distance_interval(BS1, BS2, Seq, D, Offset) :-
	 var(D), !,
	 distance(BS1, BS2, Seq, D),  range(Offset).

distance_interval(BS1, BS2, Seq, D, Offset) :-
	 distance(BS1, BS2, Seq, D1), D1 < D+Offset, D1 > D-Offset.

range(2).
range(3).
range(4).
