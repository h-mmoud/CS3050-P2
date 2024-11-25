cat(luna).
cat(simba).
cat(whiskers).

dog(buddy).
dog(max).
dog(bella).

bird(tweety).
bird(polly).

fish(nemo).
fish(dory).

animal(X) :- cat(X).
animal(X) :- dog(X).
animal(X) :- bird(X).
animal(X) :- fish(X).

pet(X) :- animal(X).
pet(X) :- cat(X).
pet(X) :- dog(X).
wild_animal(X) :- animal(X), bird(X).

can_fly(X) :- bird(X).
can_swim(X) :- fish(X).
can_swim(X) :- dog(X).

likes(X, Y) :- cat(X), fish(Y). 
likes(X, Y) :- dog(X), bird(Y).
likes(X, Y) :- dog(X), cat(Y).
likes(X, Y) :- animal(X), pet(Y).
likes(X, Y) :- bird(X), animal(Y).

dislikes(X, Y) :- fish(X), cat(Y).
dislikes(X, Y) :- bird(X), dog(Y).

?- animal(X).
?- pet(X).
?- wild_animal(X).
?- can_fly(X).
?- can_swim(X).
?- likes(X, Y).
?- dislikes(X, Y).
?- likes(bella, Y).
?- dislikes(nemo, Y).
?-.
?-.
?- pet(Y), likes(X, Y).
?- bird(X), dislikes(X, Y).