package com.obs.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "standing_instructions")
public class StandingInstruction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String instructionId;

    @Column(nullable = false, length = 100)
    private String instructionName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstructionType instructionType;

    @Column(nullable = false, length = 20)
    private String fromAccount;

    @Column(nullable = false, length = 20)
    private String toAccount;

    @Column(length = 100)
    private String beneficiaryName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstructionFrequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = true)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate nextExecutionDate;

    @Column(nullable = true)
    private LocalDateTime lastExecuted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstructionStatus status;

    @Column(nullable = false, columnDefinition = "int default 0")
    private int executionCount;

    @Column(nullable = true)
    private Integer maxExecutions;

    @Column(length = 200)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public StandingInstruction() {
        this.createdAt = LocalDateTime.now();
        this.status = InstructionStatus.ACTIVE;
        this.executionCount = 0;
    }

    public StandingInstruction(Long userId, String instructionName, InstructionType instructionType,
                             String fromAccount, String toAccount, BigDecimal amount, 
                             InstructionFrequency frequency, LocalDate startDate) {
        this();
        this.userId = userId;
        this.instructionName = instructionName;
        this.instructionType = instructionType;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.frequency = frequency;
        this.startDate = startDate;
        this.instructionId = generateInstructionId();
        this.nextExecutionDate = calculateNextExecutionDate(startDate, frequency);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getInstructionName() {
        return instructionName;
    }

    public void setInstructionName(String instructionName) {
        this.instructionName = instructionName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public InstructionType getInstructionType() {
        return instructionType;
    }

    public void setInstructionType(InstructionType instructionType) {
        this.instructionType = instructionType;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
        this.updatedAt = LocalDateTime.now();
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
        this.updatedAt = LocalDateTime.now();
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        this.updatedAt = LocalDateTime.now();
    }

    public InstructionFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(InstructionFrequency frequency) {
        this.frequency = frequency;
        this.nextExecutionDate = calculateNextExecutionDate(LocalDate.now(), frequency);
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }

    public void setNextExecutionDate(LocalDate nextExecutionDate) {
        this.nextExecutionDate = nextExecutionDate;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastExecuted() {
        return lastExecuted;
    }

    public void setLastExecuted(LocalDateTime lastExecuted) {
        this.lastExecuted = lastExecuted;
        this.updatedAt = LocalDateTime.now();
    }

    public InstructionStatus getStatus() {
        return status;
    }

    public void setStatus(InstructionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getMaxExecutions() {
        return maxExecutions;
    }

    public void setMaxExecutions(Integer maxExecutions) {
        this.maxExecutions = maxExecutions;
        this.updatedAt = LocalDateTime.now();
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    private String generateInstructionId() {
        return "SI" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    private LocalDate calculateNextExecutionDate(LocalDate baseDate, InstructionFrequency freq) {
        switch (freq) {
            case DAILY:
                return baseDate.plusDays(1);
            case WEEKLY:
                return baseDate.plusWeeks(1);
            case MONTHLY:
                return baseDate.plusMonths(1);
            case QUARTERLY:
                return baseDate.plusMonths(3);
            case YEARLY:
                return baseDate.plusYears(1);
            default:
                return baseDate.plusMonths(1);
        }
    }

    public void executeInstruction() {
        this.lastExecuted = LocalDateTime.now();
        this.executionCount++;
        this.nextExecutionDate = calculateNextExecutionDate(LocalDate.now(), this.frequency);
        this.updatedAt = LocalDateTime.now();
        
        // Check if max executions reached
        if (maxExecutions != null && executionCount >= maxExecutions) {
            this.status = InstructionStatus.COMPLETED;
        }
    }

    public void pauseInstruction() {
        this.status = InstructionStatus.PAUSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void resumeInstruction() {
        this.status = InstructionStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelInstruction(String reason) {
        this.status = InstructionStatus.CANCELLED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDue() {
        return this.status == InstructionStatus.ACTIVE && 
               this.nextExecutionDate != null && 
               !this.nextExecutionDate.isAfter(LocalDate.now());
    }

    public boolean isActive() {
        return this.status == InstructionStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "StandingInstruction{" +
                "id=" + id +
                ", instructionName='" + instructionName + '\'' +
                ", amount=" + amount +
                ", frequency=" + frequency +
                ", status=" + status +
                ", nextExecutionDate=" + nextExecutionDate +
                '}';
    }
}