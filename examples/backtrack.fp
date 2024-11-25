rabbit(white_rabbit).
rabbit(grey_rabbit).
dog(black_dog).

hungry(grey_rabbit).
hungry(white_rabbit).
hungry(black_dog).

experienced(black_dog).

philosopher(X) :- dog(X).
mindless(X) :- rabbit(X).

consumerist(X) :- mindless(X), hungry(X).
enlightened(X) :- philosopher(X), experienced(X).

sobered_by_modernity(X) :- enlightened(X), not(consumerist(X)).
stoic(X) :- hungry(X),  sobered_by_modernity(X).


?-stoic(X).
?-consumerist(X).
?-.