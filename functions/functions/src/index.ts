/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import * as logger from "firebase-functions/logger";
import { onTaskDispatched } from "firebase-functions/v2/tasks";
import { getFunctions } from "firebase-admin/functions";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { Notification } from "firebase-admin/lib/messaging";
import { onCall, HttpsError } from "firebase-functions/v2/https";

import admin = require("firebase-admin");
admin.initializeApp();

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

/**
 * The regularily scheduled main alarm.
 */
exports.scheduledAlarm = onSchedule({
  retryCount: 5,
  maxBackoffSeconds: 60,
  schedule: "every day 21:00",
  timeZone: "Europe/Berlin",
}, async (_) => {
  logger.info("Checking for period");
  // check fo period
  const pillenDoc = await admin
    .firestore()
    .collection("pillenalarm")
    .doc("prod")
    .get();
  if (!pillenDoc.data()) {
    logger.error("No pillen document found");
    return;
  }
  const periodEnd = pillenDoc.data()?.periodEnd || 0;
  if (Date.now() < periodEnd) {
    logger.info("Period active. Skipping alarm.");
    return;
  }
  logger.info("Updating current alarm");
  // update current alarm and clear previously scheduled reminder
  await admin.firestore().collection("pillenalarm").doc("prod").update({
    done: false,
    scheduledReminder: 0,
  });
  logger.info("Sending notifications");
  // send notification
  await sendNotification(pillenDoc.data()?.token, {
    title: "Pillenalarm",
    body: "Zeit die Pille zu nehmen 💊",
  });
  // schedule reminder
  await scheduleReminder();
  logger.info("Reminder scheduled");
});

/**
 * The auto enqueued reminder alarm.
 */
exports.autoReminderQueue = onTaskDispatched({
  retryConfig: {
    maxAttempts: 5,
    minBackoffSeconds: 60,
  },
}, async (_) => {
  logger.info("Executing reminder");
  // check whether current alarm is done by now
  const pillenDoc = await admin
    .firestore()
    .collection("pillenalarm")
    .doc("prod")
    .get();
  if (!pillenDoc.data()) {
    logger.error("No pillen document found");
    return;
  }
  if (pillenDoc.data()?.done) {
    logger.info("Alarm done. Cancelling reminders.");
    return;
  }
  if (pillenDoc.data()?.scheduledReminder &&
    pillenDoc.data()?.scheduledReminder > Date.now()) {
    logger.info("Manual reminder scheduled. " +
      "Cancelling auto reminders for now.");
    return;
  }
  // send reminder
  logger.info("Sending notifications");
  await sendNotification(pillenDoc.data()?.token, {
    title: "Pillenerinnerung",
    body: "Freundliche Erinnerung die Pille zu nehmen 💊",
  });
  // schedule next reminder
  await scheduleReminder();
  logger.info("Next reminder scheduled");
});

/**
 * The manually enqueued reminder alarm.
 */
exports.manReminderQueue = onTaskDispatched({
  retryConfig: {
    maxAttempts: 5,
    minBackoffSeconds: 60,
  },
}, async (req) => {
  const doc = req.data?.doc || "prod";
  logger.info(`Executing manual reminder in ${doc} environment`);
  // check whether current alarm is done by now
  const pillenDoc = await admin
    .firestore()
    .collection("pillenalarm")
    .doc(doc)
    .get();
  if (!pillenDoc.data()) {
    logger.error("No pillen document found");
    return;
  }
  if (pillenDoc.data()?.done) {
    logger.info("Alarm done. Cancelling manual reminder.");
    return;
  }
  if (pillenDoc.data()?.scheduledReminder) {
    // send reminder
    logger.info("Sending notifications");
    await sendNotification(pillenDoc.data()?.token, {
      title: "Pillenerinnerung",
      body: "Freundliche Erinnerung die Pille zu nehmen 💊",
    });
    await pillenDoc.ref.update({
      scheduledReminder: 0,
    });
  } else {
    logger.info("Manual reminder cleared. Cancelling this reminder.");
  }
  // schedule next reminder
  await scheduleReminder();
  logger.info("Next auto reminder scheduled");
});

/**
 * Called by the Android app to schedule a manual reminder.
 */
exports.scheduleDelay = onCall(async (request) => {
  const delay = request.data?.delay;
  const doc = request.data?.doc || "prod";
  if (!delay) {
    throw new HttpsError("invalid-argument", "No delay provided");
  }
  const pillenDoc = await admin
    .firestore()
    .collection("pillenalarm")
    .doc(doc)
    .get();
  if (!pillenDoc.data()) {
    throw new HttpsError("not-found", "No config document found");
  }
  const reminderTime = new Date(Date.now() + delay);
  const delayMinutes = Math.round(delay / 1000 / 60);
  logger.info(
    `Scheduling manual reminder for 
    ${reminderTime} (in ${delayMinutes} minutes)`
  );
  const queue = getFunctions().taskQueue("manReminderQueue");
  await queue.enqueue(
    {
      doc,
    },
    {
      scheduleTime: reminderTime,
    }
  );
  await pillenDoc.ref.update({
    scheduledReminder: reminderTime.getTime(),
  });
  return { delay };
});


/**
 * Sends a notification to the given token
 * @param {string} token The token to send the notification to
 * @param {Notification} notification The notification
 */
async function sendNotification(
  token: string,
  notification: Notification
) {
  logger.info("Sending notification to", token);
  await admin.messaging().send({
    notification,
    token,
  });
}

/**
 * Enqueues a reminder task.
 */
async function scheduleReminder() {
  const configDoc = await admin
    .firestore()
    .collection("pillenalarm")
    .doc("config")
    .get();
  const delayMinutes = configDoc.data() ?
    configDoc.data()?.reminderDelayMinutes : 30;
  const delay = 1000 * 60 * delayMinutes;
  const reminderTime = new Date(Date.now() + delay);
  logger.info(
    `Scheduling reminder for ${reminderTime} (in ${delayMinutes} minutes)`
  );
  const queue = getFunctions().taskQueue("autoReminderQueue");
  await queue.enqueue(
    {},
    {
      scheduleTime: reminderTime,
    }
  );
}
