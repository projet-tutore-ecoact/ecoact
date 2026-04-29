package com.project.ecoact.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "user_habits",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("user_id"),
                @Index(value = {"user_id", "habit_key"}, unique = true)
        }
)
public class HabitEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "habit_key")
    private String habitKey;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "question")
    private String question;

    @ColumnInfo(name = "frequency")
    private String frequency;

    @ColumnInfo(name = "inverted")
    private boolean inverted;

    @ColumnInfo(name = "points")
    private int points;

    @ColumnInfo(name = "max_points")
    private int maxPoints;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    @Ignore
    public HabitEntity(long userId, String habitKey, String category, String question,
                       String frequency, boolean inverted, int points, int maxPoints) {
        this.userId = userId;
        this.habitKey = habitKey;
        this.category = category;
        this.question = question;
        this.frequency = frequency;
        this.inverted = inverted;
        this.points = points;
        this.maxPoints = maxPoints;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public HabitEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getHabitKey() {
        return habitKey;
    }

    public void setHabitKey(String habitKey) {
        this.habitKey = habitKey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
