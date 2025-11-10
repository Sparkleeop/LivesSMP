## 🧡 LivesSMP


### ⚔️ Core Concept

Players begin with a configurable number of lives (default: **3**).
Every death subtracts one life. When a player’s lives reach **0**, they are **automatically banned** until another player revives them using the **Revive Crystal**, a special item crafted with rare materials.

---

### ✨ Features

✅ **Configurable Life System** - Set `starting-lives` and `max-lives` in config.
💀 **Ban on 0 Lives** - Players are banned automatically until revived.
💎 **Revive Crystal** - Fully customizable crafting recipe for reviving banned players.
♾️ **Unlimited Lives Mode** - Set `max-lives: -1` to remove all life limits.
⚙️ **Admin Commands** - `/addlives`, `/setlives`, `/removelives`, `/checklives`, `/toplives`.
💬 **Custom Messages** - All messages and prefixes are configurable with hex color (`&#RRGGBB`) support.
💾 **MySQL or YAML Storage** - Switch easily between local file or database persistence.
📊 **PlaceholderAPI Support** - Display lives and status with `%livessmp_lives%` and `%livessmp_status%`.
🪄 **Cross-Compatible** - Works on Paper, Purpur, and Spigot (1.21+).

---

### ⚙️ Commands

| Command                          | Description                                   |
| -------------------------------- | --------------------------------------------- |
| `/livessmp`                      | Shows plugin info and help                    |
| `/lives`                         | Shows your remaining lives                    |
| `/revive <player>`               | Revive a banned player using a Revive Crystal |
| `/addlives <player> <amount>`    | Add lives to a player                         |
| `/setlives <player> <amount>`    | Set a player’s lives                          |
| `/removelives <player> <amount>` | Remove lives from a player                    |
| `/checklives <player>`           | Check another player’s lives                  |
| `/toplives`                      | Shows the top players with most lives         |
| `/livessmpreload`                | Reloads plugin configuration and recipes      |

---

### 🪄 Permissions

| Permission        | Description                          | Default |
| ----------------- | ------------------------------------ | ------- |
| `livessmp.admin`  | Manage lives (add/set/remove)        | OP      |
| `livessmp.bypass` | Prevents life loss and bans          | OP      |
| `livessmp.check`  | Allows checking other players’ lives | OP      |

---

### 💬 Custom Messages

All plugin messages (join, life lost, revive, admin actions, etc.) can be customized in the config with full `&#HEXCODE` color support and placeholders like `%player%`, `%target%`, and `%lives%`.

---

### ⚡ Compatibility

✅ **Paper / Purpur / Spigot 1.21+**
⚙️ Built using the **modern Paper API**
🚀 Optimized for **performance and stability**

---

### 🐞 Issue Tracker

Found a bug or have a suggestion?
Report it on the [GitHub Issues page](https://github.com/Sparkleeop/LivesSMP/issues).
Please include:

* Your **Minecraft version** (e.g., 1.21.5)
* Your **server type** (Paper, Purpur, Spigot, etc.)
* Your **LivesSMP plugin version**
* Steps to reproduce the issue or screenshots/logs

Your feedback helps make LivesSMP better for everyone 💪

---

### 🤝 Contributing

Want to help improve LivesSMP? Awesome!
You can contribute by:

* 🧠 Submitting feature ideas in the issues tab
* 🐛 Reporting bugs and suggesting fixes
* 💻 Opening pull requests with improvements or optimizations
* 🌍 Helping with documentation or translations

Before contributing code, please:

1. Fork the repository on [GitHub](https://github.com/Sparkleeop/LivesSMP).
2. Create a new branch for your feature or fix.
3. Test your changes thoroughly on a Paper server (1.21+).
4. Submit a pull request with a clear description of your update.

Every contribution is appreciated, even small ones ❤️

---

### ❤️ Perfect For

* **SMP servers** looking to add stakes and excitement
* **Hardcore survival** or **lifesteal-like** experiences
* **Community-driven** revival mechanics
* Servers that love **custom, configurable gameplay**