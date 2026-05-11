workspace "CP cluster — three replicas with a quorum write path" {

    model {
        client = person "Client" "Writes and reads keys."

        kv = softwareSystem "Quorum KV store" "Three-node CP cluster. Writes need majority ack." {
            replicaA = container "Replica A" "Coordinator in the demo scenarios. Writes to the local store after quorum ack." "Python (cap_simulator)"
            replicaB = container "Replica B" "Voting peer. Acks if reachable; otherwise the cluster cannot form quorum."     "Python (cap_simulator)"
            replicaC = container "Replica C" "Voting peer. Same role as B."                                                  "Python (cap_simulator)"

            replicaA -> replicaB "replicates write & awaits ack"
            replicaA -> replicaC "replicates write & awaits ack"
            replicaB -> replicaA "replicates write & awaits ack (when B is coordinator)"
            replicaB -> replicaC "replicates write & awaits ack"
            replicaC -> replicaA "replicates write & awaits ack (when C is coordinator)"
            replicaC -> replicaB "replicates write & awaits ack"
        }

        client -> kv "write(x, v) — forwarded to majority via coordinator"
    }

    views {
        systemContext kv "CpClusterContext" "CP cluster — system context." {
            include *
            autolayout lr
        }
        container kv "CpClusterContainers" "CP cluster — three replicas behind a coordinator." {
            include *
            autolayout lr
        }

        styles {
            element "Person" {
                background "#dbeafe"
                color      "#1e3a5f"
                shape      Person
            }
            element "Software System" {
                background "#ede9fe"
                color      "#1e3a5f"
            }
            element "Container" {
                background "#fef9c3"
                color      "#1e3a5f"
            }
        }
    }
}
