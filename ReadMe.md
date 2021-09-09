# Remind Me

This is my second self-made Android app. It's a relatively simple reminder app, at least in comparison to what else is out there. Nevertheless, it's been quite a lot of work developing it all alone especially with my still very limited amount of experience. 

So what's in it? Probably what you'd expect, maybe even a bit less - take a look:
* You can set reminders, obviously. 
* Customize them with date, time, title, text, color and repitition. 
* They are represented as a list (RecyclerView) on the home screen. This is also my first in action Room database - I tried it once before with the dice app though that didn't really work out back then :smile:
* Edit them.
* Delete them. I wanted to include a multi-delete feature but the way RecyclerView works made the way I wanted my feature to work inconvenient - I noticed it too late, should've just used ListView, maybe I'll still adapt it to that in an later update. For now it's either delete one by one or delete everything. I personally don't find my self deleting a lot - mostly I do it directly from the notification.
* When a reminder is triggered you get a notification, from there you have a bunch of actions like stop repeating (if it's a repeating reminder), delete and edit.
* Stopped or non repeating reminders are halted, but preserved so you can come back to them if need be.

All in all I'm happy with the outcome and personally use the app all the time. I like the, in my opinion, clean design and easy to get into functionality. There's no "feature overload" for you to chew through though this could also be seen as a disadvantage for people looking for a more personalized experience.

As a little code review I can say that it isn't perfect - there are some weird decisions I made at the start of development. But there are also lots of cleanly implemented features, easy to edit and expand upon. I especially noticed both of these during a time where I developed only rarely and with relatively big breaks when I had to basically get into the code all over again. I'm happy with what I created and excited to see what comes in the future. My studies at university begin literally in two weeks :smiley:

If anyone ever reads this - have fun with what I've created! Take care :world_map:

# **Creating a Reminder:**
![Reminders_SS_1](https://github.com/LukasH77/RemindMe/blob/master/app/src/main/res/drawable/reminder_ss_1_phone_en.png)

# **List of Reminders:**
![Reminders_SS_2](https://github.com/LukasH77/RemindMe/blob/master/app/src/main/res/drawable/reminder_ss_3_phone_en.png)

# **Editing a Reminder:**
![Reminders_SS_3](https://github.com/LukasH77/RemindMe/blob/master/app/src/main/res/drawable/reminder_ss_4_phone_en.png)
