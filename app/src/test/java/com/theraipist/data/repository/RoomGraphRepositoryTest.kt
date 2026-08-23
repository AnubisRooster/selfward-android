package com.theraipist.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.theraipist.core.graph.GraphEdge
import com.theraipist.core.graph.GraphNode
import com.theraipist.data.local.TherAIpistDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RoomGraphRepositoryTest {

    private lateinit var db: TherAIpistDatabase
    private lateinit var repo: RoomGraphRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, TherAIpistDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = RoomGraphRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun savesAndLoadsNodesAndEdgesAcrossSessions() = runTest {
        val nodeA = GraphNode(id = "n_1", label = "Mother", kind = "person", createdAt = 100)
        val nodeB = GraphNode(id = "n_2", label = "Fear", kind = "emotion", createdAt = 200)
        val edge = GraphEdge(id = "e_1", sourceId = "n_1", targetId = "n_2", label = "next", weight = null)

        repo.saveNode("session1", nodeA)
        repo.saveNode("session2", nodeB)
        repo.saveEdge("session2", edge)
        repo.saveInsight("session2", "named the fear")

        val snapshot = repo.loadAll()
        assertEquals(listOf(nodeA, nodeB), snapshot.nodes)
        assertEquals(listOf(edge), snapshot.edges)
    }

    @Test
    fun replacingANodeWithTheSameIdOverwritesIt() = runTest {
        val original = GraphNode(id = "n_1", label = "Old", kind = null, createdAt = 100)
        val updated = GraphNode(id = "n_1", label = "New", kind = null, createdAt = 100)

        repo.saveNode("session1", original)
        repo.saveNode("session1", updated)

        assertEquals(listOf(updated), repo.loadAll().nodes)
    }
}
