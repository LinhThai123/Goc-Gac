<#import "template.ftl" as layout>
<@layout.emailLayout>
    <h2>${msg("passwordResetTitle")}</h2>
    
    <p>${msg("passwordResetGreeting", user.firstName!user.username)}</p>
    
    <p>${msg("passwordResetMessage")}</p>
    
    <div class="button-container">
        <a href="${link}" class="button">${msg("passwordResetButton")}</a>
    </div>
    
    <p>${msg("passwordResetAlternative")}</p>
    <div class="link-text">
        ${link}
    </div>
    
    <div class="warning">
        <strong>⚠️ ${msg("emailVerificationWarningTitle")}</strong><br>
        • ${msg("passwordResetWarningExpiry", linkExpiration)}<br>
        • ${msg("passwordResetWarningIgnore")}
    </div>
</@layout.emailLayout>

