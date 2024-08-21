const functions = require('firebase-functions');
const admin = require('firebase-admin');
const moment = require('moment-timezone');

admin.initializeApp();


exports.scheduleUserEventNotificationsCRUD = functions.firestore
    .document('users/{userId}/events/{eventId}')
    .onWrite(async (change, context) => {
        const eventId = context.params.eventId;
        const userId = context.params.userId;
        console.log('starting scheduleUserEventNotificationsCRUD')
        const eventB = change.before.data();

        // If the document was deleted
        if (!change.after.exists) {
            console.log(`Event ${eventId} for user ${userId} was deleted`);
            // Call a function to clean up scheduled notifications
            for (const day of eventB.daysOfWeek) {
                console.log('delete triggered')

                await cleanupScheduledNotifications(userId, eventId, day);
            }
            
            return null;
        }

        const event = change.after.data();
        
        console.log('1')
        console.log(event.startTime)
        console.log(event.daysOfWeek)
        
        if (!event.startTime || !event.daysOfWeek || !Array.isArray(event.daysOfWeek)) {
            console.log(`Event ${eventId} for user ${userId} is missing required fields`);
            return null;
        }
        
        console.log('2')
        const firestore = admin.firestore();

        try {
            const userDoc = await firestore.collection('users').doc(userId).get();
            console.log('3')
            
            if (!userDoc.exists) {
                console.log(`User ${userId} not found`);
                return null;
            }

            console.log('4')

            const userFcmToken = userDoc.data().fcmToken;
            if (!userFcmToken) {
                console.log(`No FCM token found for user ${userId}`);
                return null;
            }

            // Schedule notifications for each day of the week the event occurs
            for (const day of event.daysOfWeek) {
                console.log('update or add triggered')

                await scheduleNotificationForDay(userId, eventId, event, day, userFcmToken);
            }

            console.log(`Notifications scheduled for event ${eventId} of user ${userId}`);
            return null;
        } catch (error) {
            console.error(`Error scheduling notifications for event ${eventId} of user ${userId}:`, error);
            return null;
        }
    });

    async function cleanupScheduledNotifications(userId, eventId, day) {
        console.log(`Cleaning up scheduled notifications for event ${eventId} of user ${userId}`);

        let uniqueNotificationId = `${userId}-eventid-${eventId}-${day}`

        await admin.firestore().collection('scheduledNotifications').doc(uniqueNotificationId).delete();
    
        console.log(`Notification deleted for event ${eventId} of user ${userId} on ${day}`);

    }
    
    
// async function scheduleNotificationForDay(userId, eventId, event, day, userFcmToken) {
//     const dayMapping = {
//         'sunday': 0, 'monday': 1, 'tuesday': 2, 'wednesday': 3,
//         'thursday': 4, 'friday': 5, 'saturday': 6
//     };

//     console.log('scheduleNotificationForDay 1')
//     const dayNumber = dayMapping[day.toLowerCase()];
//     if (dayNumber === undefined) {
//         console.log(`Invalid day ${day} for event ${eventId} of user ${userId}`);
//         return;
//     }

//     console.log('scheduleNotificationForDay 2')
//     console.log(event.startTime)
//     const now = new Date();
//     const userTimeZone = userDoc.data().timezone;

//     // console.log(`rightNow: ${rightNow}`)

//     // const now = new Date(rightNow.toLocaleString("en-US", {timeZone: "America/New_York"}));
//     console.log(`now: ${now}`)

//     const eventTime = new Date(
//         now.getFullYear(),
//         now.getMonth(),
//         now.getDate(),
//         event.startTime.hour,
//         event.startTime.minute,
//         event.startTime.second || 0
//       );
      
//     console.log(eventTime)

//     const scheduleTime = new Date(eventTime);
//     console.log(scheduleTime)

//     scheduleTime.setDate(scheduleTime.getDate() + (7 + dayNumber - eventTime.getDay()) % 7);
//     console.log(scheduleTime)

//     console.log('scheduleNotificationForDay 3')

//     // Set the time part
//     scheduleTime.setHours(eventTime.getHours());
//     scheduleTime.setMinutes(eventTime.getMinutes());
//     scheduleTime.setSeconds(0);
//     scheduleTime.setMilliseconds(0);
//     console.log('scheduleNotificationForDay 4')

//     // Subtract 1 hour to send notification before the event
//     scheduleTime.setTime(scheduleTime.getTime());
//     console.log('scheduleNotificationForDay 5')
//     console.log(scheduleTime)

//     const scheduleDelta = scheduleTime.getTime() - Date.now();
//     if (scheduleDelta < 0) {
//         scheduleTime.setDate(scheduleTime.getDate() + 7); // Schedule for next week if in the past
//     }
//     console.log('scheduleNotificationForDay 6')

//     console.log(scheduleTime)

//     console.log('invoke toUTC() from schedule notification for day')
//     const scheduleTimeUTC =  await toUTC(scheduleTime, userId)

//     // to make it easier to locate a scheduled notification
//     let uniqueNotificationId = `${userId}-eventid-${eventId}-${day}`

//     // Create a scheduled task for this notification
//     await admin.firestore().collection('scheduledNotifications').doc(uniqueNotificationId).set({
//         userId: userId,
//         eventId: eventId,
//         scheduledTime: admin.firestore.Timestamp.fromDate(scheduleTimeUTC),
//         day: day,
//         fcmToken: userFcmToken
//     });

//     console.log(`Notification scheduled for event ${eventId} of user ${userId} on ${day} at ${scheduleTime}`);
// }

async function scheduleNotificationForDay(userId, eventId, event, day, userFcmToken) {
    const dayMapping = {
        'sunday': 0, 'monday': 1, 'tuesday': 2, 'wednesday': 3,
        'thursday': 4, 'friday': 5, 'saturday': 6
    };

    const dayNumber = dayMapping[day.toLowerCase()];
    if (dayNumber === undefined) {
        console.log(`Invalid day ${day} for event ${eventId} of user ${userId}`);
        return;
    }

    // Get user's timezone
    const firestore = admin.firestore();
    const userDoc = await firestore.collection('users').doc(userId).get();
    const userTimeZone = userDoc.data().timezone;

    // Get current time in user's timezone
    const nowInUserTZ = moment().tz(userTimeZone);
    console.log(`Current time in user's timezone (${userTimeZone}):`, nowInUserTZ.format());

    // Create event time in user's timezone
    const eventTimeInUserTZ = moment.tz({
        year: nowInUserTZ.year(),
        month: nowInUserTZ.month(),
        date: nowInUserTZ.date(),
        hour: event.startTime.hour,
        minute: event.startTime.minute,
        second: event.startTime.second || 0
    }, userTimeZone);

    console.log('Event time in user timezone:', eventTimeInUserTZ.format());

    // Adjust to the next occurrence of the specified day
    while (eventTimeInUserTZ.day() !== dayNumber) {
        eventTimeInUserTZ.add(1, 'day');
    }

    // Subtract 1 hour for notification
    const notificationTimeInUserTZ = eventTimeInUserTZ

    // If the notification time is in the past, schedule for next week
    if (notificationTimeInUserTZ.isBefore(nowInUserTZ)) {
        notificationTimeInUserTZ.add(7, 'days');
    }

    console.log('Notification time in user timezone:', notificationTimeInUserTZ.format());

    // Convert notification time to UTC for storage
    const notificationTimeUTC = notificationTimeInUserTZ.utc();
    console.log('Notification time in UTC:', notificationTimeUTC.format());

    // Create a unique notification ID
    let uniqueNotificationId = `${userId}-eventid-${eventId}-${day}`;

    // Schedule the notification
    await admin.firestore().collection('scheduledNotifications').doc(uniqueNotificationId).set({
        userId: userId,
        eventId: eventId,
        scheduledTime: admin.firestore.Timestamp.fromDate(notificationTimeUTC.toDate()),
        day: day,
        fcmToken: userFcmToken
    });

    console.log(`Notification scheduled for event ${eventId} of user ${userId} on ${day} at ${notificationTimeUTC.format()}`);
}


// This function runs every minute to check and send due notifications
exports.sendScheduledNotifications = functions.pubsub.schedule('every 1 minutes').onRun(async (context) => {
    const firestore = admin.firestore();
    const now = admin.firestore.Timestamp.now();
    console.log(now)
    console.log(now.toDate())


    const query = firestore.collection('scheduledNotifications')
        .where('scheduledTime', '<=', now)
        .limit(100); // Process in batches to avoid timeout

    const snapshot = await query.get();

    const promises = [];
    snapshot.forEach((doc) => {
        const data = doc.data();
        console.log(data)
        promises.push(sendNotification(data, doc.id));
    });

    await Promise.all(promises);
    console.log(`Processed ${promises.length} notifications`);
    return null;
});

async function sendNotification(data, docId) {
    const firestore = admin.firestore();
    
    try {
        const eventDoc = await firestore.collection('users').doc(data.userId).collection('events').doc(data.eventId).get();
        if (!eventDoc.exists) {
            console.log(`Event ${data.eventId} for user ${data.userId} no longer exists, deleting scheduled notification`);
            await firestore.collection('scheduledNotifications').doc(docId).delete();
            return;
        }

        const event = eventDoc.data();
        const message = {
            notification: {
                title: 'Upcoming Event Reminder',
                body: `Reminder: ${event.title} starts in 1 hour`
            },
            token: data.fcmToken
        };

        await admin.messaging().send(message);
        console.log(`Sent notification for event ${data.eventId} to user ${data.userId}`);

        // Reschedule for next week
        const nextSchedule = new Date(data.scheduledTime.toDate());
        nextSchedule.setDate(nextSchedule.getDate() + 7);

        await firestore.collection('scheduledNotifications').doc(docId).update({
            scheduledTime: admin.firestore.Timestamp.fromDate(nextSchedule)
        });

        console.log(`Rescheduled notification for event ${data.eventId} of user ${data.userId} to ${nextSchedule}`);
    } catch (error) {
        console.error(`Error processing notification for event ${data.eventId} of user ${data.userId}:`, error);
    }
}

