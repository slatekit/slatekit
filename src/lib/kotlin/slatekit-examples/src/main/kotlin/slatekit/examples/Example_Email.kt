/**
<slate_header>
author: Kishore Reddy
url: www.github.com/code-helix/slatekit
copyright: 2015 Kishore Reddy
license: www.github.com/code-helix/slatekit/blob/master/LICENSE.md
desc: A tool-kit, utility library and server-backend
usage: Please refer to license on github for more info.
</slate_header>
 */

package slatekit.examples


//<doc:import_required>
import slatekit.notifications.email.EmailMessage
import slatekit.notifications.email.EmailService
import slatekit.notifications.email.SendGrid
//</doc:import_required>

//<doc:import_examples>
import slatekit.common.templates.Template
import slatekit.common.templates.TemplatePart
import slatekit.common.templates.Templates
import slatekit.results.Try

import slatekit.common.conf.Config
import slatekit.common.info.ApiLogin

import slatekit.common.io.Uris
import slatekit.common.types.Vars
import slatekit.common.ext.env
import slatekit.results.builders.Tries
import kotlinx.coroutines.runBlocking
import slatekit.common.ext.orElse

//</doc:import_examples>


class Example_Email : Command("auth") {

    override fun execute(request: CommandRequest): Try<Any> {
        //<doc:setup>
        // Setup 1: Getting Api key/login info from config
        // Load the config file from slatekit directory in user_home directory
        // e.g. {user_home}/slatekit/conf/sms.conf
        // NOTE: It is safer/more secure to store config files there.
        val conf = Config.of("~/.slatekit/conf/email.conf")
        val apiKey1 = conf.apiLogin("email")

        // Setup 2: Get the api key either through conf or explicitly
        val apiKey2 = ApiLogin("17181234567", "ABC1234567", "password", "dev", "sendgrid-email")

        // Setup 3a: Setup the email service ( basic ) with api key
        val apiKey = apiKey1 ?: apiKey2
        val emailService1 = SendGrid(apiKey.key, apiKey.pass, apiKey.account)

        // Setup 3b: Setup the sms service with support for templates
        val templates = Templates.build(
                templates = listOf(
                        Template("email_welcome", Uris.readText("~/slatekit/templates/email_welcome.txt") ?: ""),
                        Template("email_pass", """
                         Hi @{user.name},

                        <p>
                        Your code for @{app.name} is @{app.code}.
                        </p>

                        Regards,
                        @{app.from}
                        @{app.tagline}
                     """.trimIndent())
                ),
                subs = listOf(
                        "app.name" to { s: TemplatePart -> "My App" },
                        "app.from" to { s: TemplatePart -> "My App Team" },
                        "app.tagline" to { s:TemplatePart -> "My App tagline" }
                )
        )
        val emailService2 = SendGrid(apiKey.key, apiKey.pass, apiKey.account, templates)
        val email: EmailService = emailService2
        //</doc:setup>

        //<doc:examples>
        runBlocking {
            // Sample email ( loaded from environment variable for test/example purposes )
            val toAddress = "SLATEKIT_EXAMPLE_EMAIL".env().orElse("user1@gmail.com")

            // Use case 1: Send a confirmation code to the U.S. to verify a users phone number.
            val result1 = email.send(toAddress, "Welcome to MyApp.com 1", "showWelcome!", false)

            // Use case 2: Send using a constructed message object
            email.sendSync(EmailMessage(toAddress, "Welcome to MyApp.com 2", "showWelcome!", false))

            // Use case 3: Send message using one of the setup templates
            val result2 = email.sendTemplate("email_welcome", toAddress, "Welcome to MyApp.com 3", true,
                    Vars(listOf(
                            "app.confirmUrl" to "https://www.myapp.com/confirm?abc=123",
                            "user.name" to "user1",
                            "user.email" to "user1@gmail.com",
                            "app.code" to "ABC123"
                    )))
        }
        //</doc:examples>

        return Tries.success("Ok")
    }
}
