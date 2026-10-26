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
💾 **MySQL or YAML Storage** - Switch easily between local file or database persistence (`plugins/LivesSMP/data.yml`).
📊 **PlaceholderAPI Support** - Display lives and status with `%livessmp_lives%` and `%livessmp_status%`.
🪄 **Cross-Compatible** - One jar for Paper, Purpur, and Folia (1.21.x – 26.x).

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
| `/withdrawlife <amount>`         | Withdraw lives into tradeable Life Shards     |
| `/toplives`                      | Shows the top players with most lives         |
| `/livessmpreload`                | Reloads plugin configuration and recipes      |

> All commands can also be used as `/livessmp <subcommand>` (for example, `/livessmp toplives`).

---

### 🪄 Permissions

| Permission        | Description                          | Default |
| ----------------- | ------------------------------------ | ------- |
| `livessmp.admin`  | Manage lives (add/set/remove)        | OP      |
| `livessmp.bypass` | Prevents life loss and bans          | OP      |
| `livessmp.check`  | Allows checking other players’ lives | OP      |
| `livessmp.withdraw` | Allows withdrawing lives into items | true   |

---

### 💬 Custom Messages

All plugin messages (join, life lost, revive, admin actions, etc.) can be customized in the config with full `&#HEXCODE` color support and placeholders like `%player%`, `%target%`, and `%lives%`.

---

### ⚡ Compatibility

✅ **Paper / Purpur / Folia 1.21.x – 26.x** (single jar)
🪶 **Folia-ready** - all scheduling uses the region-aware scheduler API
☕ **Java 21+** at runtime
⚙️ Built against the **Paper 26.3 API**, compiled to Java 21 bytecode
🚀 Optimized for **performance and stability**

> Building from source requires **JDK 25** because the 26.3 Paper API jar targets Java 25; the produced plugin still runs on Java 21.

---

### 🐞 Issue Tracker

Found a bug or have a suggestion?
Report it on the [GitHub Issues page](https://github.com/Sparkleeop/LivesSMP/issues).
Please include:

* Your **Minecraft version** (e.g., 26.3)
* Your **server type** (Paper, Purpur, Folia, etc.)
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
3. Test your changes thoroughly on a Paper or Folia server (26.3+).
4. Submit a pull request with a clear description of your update.

Every contribution is appreciated, even small ones ❤️

---

### ❤️ Perfect For

* **SMP servers** looking to add stakes and excitement
* **Hardcore survival** or **lifesteal-like** experiences
* **Community-driven** revival mechanics
* Servers that love **custom, configurable gameplay**