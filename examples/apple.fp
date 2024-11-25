gotmoney(a).
gotcredit(b).
gamble(X) :- gotmoney(X), cut.
gamble(X) :- gotcredit(X), gotmoney(X).
?- gamble(b).
?- gamble(a).