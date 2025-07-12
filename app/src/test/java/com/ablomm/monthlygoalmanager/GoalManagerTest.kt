package com.ablomm.monthlygoalmanager

import org.junit.Test
import org.junit.Assert.*
import java.util.UUID

/**
 * Unit tests for the Monthly Goal Manager data models
 */
class GoalManagerTest {
    
    @Test
    fun goalItem_creation_isCorrect() {
        val goalItem = GoalItem(
            title = "Test Goal",
            detailedDescription = "Test Description",
            targetValue = "100%",
            currentProgress = 50,
            priority = GoalPriority.High
        )
        
        assertEquals("Test Goal", goalItem.title)
        assertEquals("Test Description", goalItem.detailedDescription)
        assertEquals("100%", goalItem.targetValue)
        assertEquals(50, goalItem.currentProgress)
        assertEquals(GoalPriority.High, goalItem.priority)
        assertFalse(goalItem.isCompleted)
    }
    
    @Test
    fun goalItem_completion_update() {
        val goalItem = GoalItem(
            title = "Test Goal",
            currentProgress = 0,
            isCompleted = false
        )
        
        val completedGoal = goalItem.copy(isCompleted = true, currentProgress = 100)
        
        assertTrue(completedGoal.isCompleted)
        assertEquals(100, completedGoal.currentProgress)
    }
    
    @Test
    fun goalsViewModel_updateGoal_works() {
        val viewModel = GoalsViewModel()
        val originalGoals = viewModel.goalList.value
        
        // Get first goal and update it
        val firstGoal = originalGoals.first()
        val updatedGoal = firstGoal.copy(title = "Updated Title", currentProgress = 75)
        
        viewModel.updateGoalItem(updatedGoal)
        
        val updatedGoals = viewModel.goalList.value
        val foundGoal = updatedGoals.find { it.id == firstGoal.id }
        
        assertNotNull(foundGoal)
        assertEquals("Updated Title", foundGoal!!.title)
        assertEquals(75, foundGoal.currentProgress)
    }
    
    @Test
    fun goalsViewModel_getGoalById_works() {
        val viewModel = GoalsViewModel()
        val firstGoal = viewModel.goalList.value.first()
        
        val retrievedGoal = viewModel.getGoalById(firstGoal.id)
        
        assertNotNull(retrievedGoal)
        assertEquals(firstGoal.id, retrievedGoal!!.id)
        assertEquals(firstGoal.title, retrievedGoal.title)
    }
    
    @Test
    fun goalsViewModel_getGoalById_nonExistent_returnsNull() {
        val viewModel = GoalsViewModel()
        val nonExistentId = UUID.randomUUID()
        
        val retrievedGoal = viewModel.getGoalById(nonExistentId)
        
        assertNull(retrievedGoal)
    }
}