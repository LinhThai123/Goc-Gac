<#import "template.ftl" as layout>
<@layout.emailLayout>
    <h2>${msg("updatePasswordTitle")}</h2>
    
    <p>${msg("updatePasswordGreeting", user.firstName!user.username)}</p>
    
    <p>${msg("updatePasswordMessage")}</p>
    
    <div class="warning">
        <strong>⚠️ ${msg("emailVerificationWarningSecurity")}</strong><br>
        ${msg("updatePasswordContact")}
    </div>
</@layout.emailLayout>

