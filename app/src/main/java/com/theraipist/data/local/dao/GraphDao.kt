package com.theraipist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.theraipist.data.local.entity.GraphEdgeEntity
import com.theraipist.data.local.entity.GraphNodeEntity

@Dao
interface GraphDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: GraphNodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEdge(edge: GraphEdgeEntity)

    @Query("SELECT * FROM graph_nodes WHERE sessionId = :sessionId")
    suspend fun getNodes(sessionId: String): List<GraphNodeEntity>

    @Query("SELECT * FROM graph_edges WHERE sessionId = :sessionId")
    suspend fun getEdges(sessionId: String): List<GraphEdgeEntity>
}
