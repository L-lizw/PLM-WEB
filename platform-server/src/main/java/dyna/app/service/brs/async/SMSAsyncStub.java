package dyna.app.service.brs.async;

import dyna.app.service.AbstractServiceStub;
import dyna.app.service.brs.async.AsyncImpl;
import dyna.app.service.brs.sms.SMSImpl;
import dyna.common.dto.EmailServer;
import dyna.common.dto.Mail;
import dyna.common.dto.aas.User;
import dyna.common.log.DynaLogger;
import dyna.common.systemenum.LanguageEnum;
import dyna.common.util.SetUtils;
import dyna.common.util.StringUtils;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author Lizw
 * @date 2022/1/28
 **/
@Component
public class SMSAsyncStub extends AbstractServiceStub<AsyncImpl>
{
	protected  void sendMail(Mail mail, List<String> toUserGuidList, LanguageEnum languageEnum)
	{
		try
		{

			EmailServer currentServer = this.stubService.getSms().getEmailServer();

			if (currentServer == null)
			{
				return;
			}

			String warningText = null;
			if (currentServer.isShowWarn())
			{
				warningText = this.stubService.getMsrm().getMSRString("ID_APP_MAIL_WARNING_CONTENT", languageEnum.toString());
			}

			if (!SetUtils.isNullList(toUserGuidList))
			{
				for (String toUserGuid : toUserGuidList)
				{
					if (toUserGuid != null)
					{
						mail.setReceiveUser(toUserGuid);
						this.sendMail(currentServer, mail, warningText);
					}
				}
			}

		}
		catch (Throwable e)
		{
			DynaLogger.error("run send mail:", e);
		}
	}
	private void sendMail(EmailServer currentServer, Mail mail, String warningText)
	{

		Properties properties = null;
		Session mailSession = null;
		MimeMessage mailMessage = null;
		String subject = (String) mail.get(Mail.TITLE);
		if (subject == null)
		{
			subject = "";
		}
		String content = (String) mail.get(Mail.CONTENTS);
		if (content == null)
		{
			content = "";
		}

		String toUserGuid = mail.getReceiveUser();

		try
		{

			User user = this.stubService.getAas().getUser(toUserGuid);
			if (currentServer == null || !this.stubService.getPos().isReceiveEmail(user.getGuid()) || StringUtils.isNullString(user.getEmail()))
			{
				return;
			}
			if (currentServer.isShowWarn() && !StringUtils.isNullString(warningText))
			{
				content = content + "\r\n" + warningText;
			}

			properties = new Properties();
			// ?????????????????????
			properties.put("mail.smtp.host", currentServer.getSMTP());
			if (StringUtils.isNullString(currentServer.getPassword()))
			{
				// ???????????????
				mailSession = Session.getInstance(properties, null);
			}
			else
			{
				// ????????????
				properties.put("mail.smtp.auth", "true");
				// ????????????????????????????????????
				mailSession = Session.getInstance(properties, new Authenticator()
				{
					@Override
					public PasswordAuthentication getPasswordAuthentication()
					{
						return new PasswordAuthentication(currentServer.getUserName(), currentServer.getPassword());
					}
				});
			}
			// mailSession.setDebug(true);
			// ??????????????????
			mailMessage = new MimeMessage(mailSession);
			// ?????????
			mailMessage.setFrom(new InternetAddress(currentServer.getFromAddress()));
			// ?????????
			mailMessage.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(user.getEmail()));
			// ??????
			// MimeUtility.encodeText(subject, "UTF-8", "B");
			mailMessage.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
			// ??????
			mailMessage.setText(content, "UTF-8");
			// ????????????
			mailMessage.setSentDate(new Date());
			// ????????????
			mailMessage.saveChanges();

			// ??????
			// Thread sendThread = new Thread(new SendEmailThread(mailMessage));
			// sendThread.start();

			try
			{
				Transport.send(mailMessage);
				DynaLogger.debug("Send e-mail successful, user:" + user.getUserName() + " subject:" + subject + " content:" + content);
			}
			catch (MessagingException e)
			{
				DynaLogger.error("Send e-mail error: " + e + ", " + e.getMessage());
			}

		}
		catch (Exception e)
		{
			DynaLogger.error("Send e-mail error: " + e + ", " + e.getMessage());
		}
	}

}
