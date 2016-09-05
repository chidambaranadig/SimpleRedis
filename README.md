# SimpleRedis

A simplified in-memory database similar to REDIS.

### Data Commands
* SET
* GET
* UNSET
* NUMEQUALTO
* END

### Transaction Commands
* BEGIN
* ROLLBACK
* COMMIT

### Example Run
##### INPUTS
```
SET A 10
GET A
SET B 10
GET B
NUMEQUALTO 10
UNSET A
NUMEQUALTO 10
END
```
##### OUTPUT
```
SET A 10
GET A
> 10
SET B 10
GET B
> 10
NUMEQUALTO 10
> 2
UNSET A
NUMEQUALTO 10
> 1
END
```
### Implementation

* The Key-Value pairs are stored in-memory using a TreeMap.
* The Runtime Complexity for Read, Insert, Update and Delete is *O(log n)*.
* A Database Class is implemented, with wrapper functions for Read, Write and Delete.
* A CLI Class is implemented to simulate the REPL CLI Interface.
* The CLI Class checks for valid commands and their syntax.