package com.job.security;
public class RegisterRequest {
    private String email;
    private String password;
    private String role; 
    private String name; 
    private String companyName;

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public String getCompanyName() { return companyName; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setName(String name) { this.name = name; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
}
