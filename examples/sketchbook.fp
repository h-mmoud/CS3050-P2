cat(prof_oreo).
cat(sir_nut).
dog(olive).
animal(X) :- cat(X).
animal(X) :- dog(X).
?- animal(prof_oreo).
?- animal(X).
?- .