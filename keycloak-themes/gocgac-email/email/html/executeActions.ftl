<#import "template.ftl" as layout>
<@layout.emailLayout>
  <table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f6f9fc; padding: 20px 0;">
    <tr>
      <td align="center">
        <table width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
          
          <!-- Header -->
          <tr>
            <td align="center" style="padding: 40px 30px 20px; background: linear-gradient(135deg, #007bff, #0056b3);">
              <h1 style="color: #ffffff; font-size: 28px; margin: 20px 0 10px; font-family: Arial, Helvetica, sans-serif;">
                Chào mừng đến với GocGac HTX!
              </h1>
              <p style="color: #e0e0e0; font-size: 16px; margin: 0;">
                Nền tảng thương mại điện tử hợp tác xã Việt Nam
              </p>
            </td>
          </tr>
          
          <!-- Content -->
          <tr>
            <td style="padding: 40px 30px; font-family: Arial, Helvetica, sans-serif; color: #333333;">
              <h2 style="color: #007bff; font-size: 24px; margin: 0 0 20px;">Xin chào ${user.firstName!user.username!'bạn'}!</h2>
              
              <p style="font-size: 16px; line-height: 1.6; margin: 0 0 20px;">
                Cảm ơn bạn đã đăng ký tài khoản tại <strong>GocGac HTX</strong>. Chúng tôi rất vui khi được đồng hành cùng bạn trong hành trình mua sắm và hỗ trợ các sản phẩm hợp tác xã chất lượng.
              </p>
              
              <p style="font-size: 16px; line-height: 1.6; margin: 0 0 30px;">
                Để kích hoạt tài khoản và bắt đầu trải nghiệm ngay, vui lòng xác thực email bằng cách nhấn nút bên dưới:
              </p>
              
              <!-- CTA Button -->
              <table border="0" cellpadding="0" cellspacing="0" style="margin: 0 auto;">
                <tr>
                  <td align="center" style="border-radius: 6px; background-color: #007bff;">
                    <a href="${link}" target="_blank" style="display: inline-block; padding: 16px 40px; color: #ffffff; font-size: 18px; font-weight: bold; text-decoration: none; border-radius: 6px;">
                      XÁC THỰC EMAIL NGAY
                    </a>
                  </td>
                </tr>
              </table>
              <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;" />
              
              <p style="font-size: 14px; color: #555555; line-height: 1.6;">
                Trân trọng,<br/>
                <strong>Đội ngũ GocGac HTX</strong><br/>
                Website: <a href="https://gocgac-htx.vn" style="color: #007bff;">gocgac-htx.vn</a><br/>
                Hỗ trợ: support@gocgac-htx.vn | Hotline: 1900 1234
              </p>
            </td>
          </tr>
          
          <!-- Footer -->
          <tr>
            <td align="center" style="padding: 20px 30px; background-color: #f8f9fa; font-size: 12px; color: #777777;">
              <p style="margin: 0 0 10px;">
                © 2025 GocGac HTX - Nền tảng thương mại điện tử hợp tác xã Việt Nam. All rights reserved.
              </p>
              <p style="margin: 0;">
                Bạn nhận email này vì đã đăng ký tài khoản tại GocGac HTX.
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</@layout.emailLayout>

