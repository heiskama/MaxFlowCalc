# MaxFlowCalc
Calculate maximum flow in a flow network in O(VE^2) time. Implementation of the Edmonds-Karp algorithm.

For example:

```
Flow in the beginning: 0

      0/6
(1) ------> (2)
 |           |
 | 0/5       | 0/4 
 V           V
(3) ------> (4)
      0/3


Flow in the end: 7
	
      4/6
(1) ------> (2)
 |           |
 | 3/5       | 4/4 
 V           V
(3) ------> (4)
      3/3

```
