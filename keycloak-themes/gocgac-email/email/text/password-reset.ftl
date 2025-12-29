${msg("passwordResetTitle")}

${msg("passwordResetGreeting", user.firstName!user.username)}

${msg("passwordResetMessage")}

${msg("emailVerificationLinkLabel")}:
${link}

${msg("emailVerificationWarningTitle")}:
- ${msg("passwordResetWarningExpiry", linkExpiration)}
- ${msg("passwordResetWarningIgnore")}

---
${msg("emailFooterDescription")}
${msg("emailFooterAutoMessage")}
${msg("emailFooterContact")}: support@gocgac.com

© 2024 GocGac. ${msg("emailFooterRights")}

