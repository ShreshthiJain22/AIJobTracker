package com.example.job_tracker.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String role;

   @Column(columnDefinition = "TEXT")
private String jdLink;

    @Column(nullable = false)
    private String status;

    private LocalDate appliedDate;

    private String source;

    private String contactPerson;

    private boolean referralRequested;

    private LocalDate referralRequestedDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String emailUsed;

    private boolean referralReceived;

    private LocalDate lastNudgeSentDate;
    
    @ManyToOne
@JoinColumn(name = "user_id")
private User user;

    public Long getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getJdLink() {
        return jdLink;
    }
    public void setJdLink(String jdLink) {
        this.jdLink = jdLink;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }
    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public String getContactPerson() {
        return contactPerson;
    }
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public boolean isReferralRequested() {
        return referralRequested;
    }
    public void setReferralRequested(boolean referralRequested) {
        this.referralRequested = referralRequested;
    }

    public LocalDate getReferralRequestedDate() {
        return referralRequestedDate;
    }
    public void setReferralRequestedDate(LocalDate referralRequestedDate) {
        this.referralRequestedDate = referralRequestedDate;
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
    public boolean isReferralReceived() {
    return referralReceived;
}
public void setReferralReceived(boolean referralReceived) {
    this.referralReceived = referralReceived;
}

    public LocalDate getLastNudgeSentDate() {
        return lastNudgeSentDate;
    }
    public void setLastNudgeSentDate(LocalDate lastNudgeSentDate) {
        this.lastNudgeSentDate = lastNudgeSentDate;
    }
   
   

        public String getEmailUsed() {
        return emailUsed;
    }
    public void setEmailUsed(String emailUsed) {
        this.emailUsed = emailUsed;
    }


    public User getUser() {
    return user;
}
public void setUser(User user) {
    this.user = user;
}

}