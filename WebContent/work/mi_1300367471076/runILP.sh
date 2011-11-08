% Background model 

#!/bin/bash
export PATH=!swipl.install.dir!:$PATH
swipl <<EOF
['C:/(Oxy/workspace_Helios/ModuleInducer/scripts/aleph/aleph'].
[ilpBackground].
read_all(moduleInducer), induce, halt.
EOF
