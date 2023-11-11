# spamtoy

#### Primary class: Batch
- Gets as input an array of strings
- Uses Nilsimsa hash to determine similarity between one string and each of
all others
- Normalizes and stores similarity scores in a lower triangular matrix / 2D
array
- Calculates the spam probability of one string as N/D where
  - N is the number of other strings whose scores with it are higher than 80%
  of the maximum normalized score
  - D is the total number of other strings

#### Tests
- For each test case:
  - Generate spam strings with 3 templates - 60% with the 1st, 20% the 2nd, the
  rest the 3rd
    - Strings with the same template differ by recipient name, sender name, and
    an amount of money
    - Recipient names and sender names are picked randomly from
    `recipients.txt` and `senders.txt`, respectively
    - Amounts of money are generated randomly
  - Generate ham strings
    - Length is picked randomly between 30 and 150 words
    - Words are picked randomly from `words.txt`
- Cases
  - 50% spams, 50% hams
  - 80% spams, 20% hams
  - 10% spams, 90% hams