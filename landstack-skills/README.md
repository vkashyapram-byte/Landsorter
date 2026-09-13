# LandStack Agent Skills (for Google Antigravity)

Five Agent Skills tailored to the LandStack Android build. Antigravity loads
each skill's `SKILL.md` description at session start, and auto-activates the
full skill only when a request semantically matches it -- so these stay out
of the way until they're actually relevant.

| Skill | Triggers on |
|---|---|
| `landstack-architecture` | Any code addition/refactor -- MVVM + Repository + Hilt rules |
| `navic-gnss-integration` | Any GNSS/location/NavIC code |
| `land-parcel-schema` | Any parcel field, screen, or mock data change |
| `govtech-compose-ui` | Any new/changed Compose screen |
| `gradle-build-verification` | Before declaring the project "builds cleanly" |

## Install (project scope -- recommended)

1. In your LandStack project folder (e.g. `C:\Users\<you>\AndroidStudioProjects\LandStack`),
   create a folder named `.agents\skills\` if it doesn't already exist.
2. Copy all 5 skill folders from this pack (`landstack-architecture/`,
   `navic-gnss-integration/`, `land-parcel-schema/`, `govtech-compose-ui/`,
   `gradle-build-verification/`) into that `.agents\skills\` folder, so it
   looks like:

   ```
   LandStack\
     .agents\
       skills\
         landstack-architecture\SKILL.md
         navic-gnss-integration\SKILL.md
         land-parcel-schema\SKILL.md
         govtech-compose-ui\SKILL.md
         gradle-build-verification\SKILL.md
   ```
3. Open (or re-open) the LandStack project in Antigravity, then ask it
   "What skills are available?" to confirm all 5 are listed.

That's it -- no restart of Android Studio needed, and no extra config file.
Antigravity will pull each skill's full instructions into context only when
a task matches its description.
