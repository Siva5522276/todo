require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const twilio = require('twilio');

const app = express();
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

const PORT = process.env.PORT || 3000;

// Root endpoint
app.get('/', (req, res) => {
    res.send('SmartTodo Backend is running! 💛');
});

// Endpoint to trigger a WhatsApp reminder
app.post('/trigger-reminder', async (req, res) => {
    const { to, title, time, userName, twilioSid, twilioToken, twilioFrom } = req.body;
    
    // We can use the user's provided credentials or our own environment variables
    const client = twilio(twilioSid || process.env.TWILIO_SID, twilioToken || process.env.TWILIO_TOKEN);

    try {
        const message = await client.messages.create({
            body: `Hey ${userName}! 🌟 Just your SmartTodo mama checking in —\nDon't forget: ${title}\nYou set this reminder for ${time}.\nReply "done" if you've handled it, or "snooze 30" to remind you in 30 mins.\nLove you, stay on track! 💪`,
            from: `whatsapp:${twilioFrom || process.env.TWILIO_WHATSAPP_NUMBER}`,
            to: `whatsapp:${to}`
        });
        res.status(200).json({ success: true, sid: message.sid });
    } catch (error) {
        console.error('Error sending WhatsApp:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// WhatsApp Webhook - Handle user replies
app.post('/whatsapp-webhook', (req, res) => {
    const incomingMsg = req.body.Body.toLowerCase().trim();
    const from = req.body.From;
    const twiml = new twilio.twiml.MessagingResponse();

    if (incomingMsg === 'done') {
        twiml.message("That's my good child! I've marked it as done. Proud of you! 💛");
        // Here we would ideally notify the app via a socket or another webhook
    } else if (incomingMsg.startsWith('snooze')) {
        twiml.message("Okay, I'll remind you again soon. Don't make me wait too long! 😊");
    } else {
        twiml.message("I'm not sure what that means, honey. Just say 'done' or 'snooze'!");
    }

    res.writeHead(200, { 'Content-Type': 'text/xml' });
    res.end(twiml.toString());
});

// Trigger the "Speak English" Motherly Call
app.post('/trigger-scold', async (req, res) => {
    const { to, userName, twilioSid, twilioToken, twilioFrom } = req.body;
    const client = twilio(twilioSid || process.env.TWILIO_SID, twilioToken || process.env.TWILIO_TOKEN);

    try {
        const call = await client.calls.create({
            url: `${req.protocol}://${req.get('host')}/voice-webhook?reason=scold&userName=${encodeURIComponent(userName)}`,
            to: to,
            from: twilioFrom || process.env.TWILIO_PHONE_NUMBER
        });
        res.status(200).json({ success: true, sid: call.sid });
    } catch (error) {
        console.error('Error triggering scold call:', error);
        res.status(500).json({ success: false, error: error.message });
    }
});

// Voice Webhook - Motherly TwiML call
app.post('/voice-webhook', (req, res) => {
    const { title, userName, reason } = req.query;
    const twiml = new twilio.twiml.VoiceResponse();

    if (reason === 'scold') {
        twiml.say({
            voice: 'Polly.Joanna',
            rate: '90%'
        }, `Hey sweetheart! This is your SmartTodo mama. I only understand English right now. Could you please say your todo in English? I'm waiting for you! 😊`);
    } else {
        const say = twiml.say({
            voice: 'Polly.Joanna',
            rate: '90%'
        }, `Hello ${userName || 'sweetheart'}! This is your SmartTodo reminder calling. You have a pending task: ${title}. I sent you a WhatsApp message but you didn't reply, so here I am calling you like a good mama would! Press 1 if you have completed this task. Press 2 to snooze for 30 minutes. Let's get it done!`);
        
        twiml.gather({
            numDigits: 1,
            action: '/handle-keypress',
            timeout: 10
        });
    }

    res.type('text/xml');
    res.send(twiml.toString());
});

// Handle Digit Press during call
app.post('/handle-keypress', (req, res) => {
    const digit = req.body.Digits;
    const twiml = new twilio.twiml.VoiceResponse();

    if (digit === '1') {
        twiml.say("Wonderful! I'm so glad you finished it. I'll mark it as complete. Bye for now!");
    } else if (digit === '2') {
        twiml.say("Okay, I'll remind you in 30 minutes. Don't forget again!");
    } else {
        twiml.say("I didn't catch that, but I'll check back later. Love you!");
    }

    res.type('text/xml');
    res.send(twiml.toString());
});

app.listen(PORT, () => {
    console.log(`SmartTodo Backend listening on port ${PORT}`);
});
