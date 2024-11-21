proposed(hastings, cynthia).
proposed(lawrence, cynthia).

accepted(cynthia, lawrence).

betrothed(X, Y) :- proposed(X, Y), accepted(Y, X).

?- betrothed(X, Y).

