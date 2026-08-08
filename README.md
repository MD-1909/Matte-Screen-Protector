# Matte Screen Filter

An Android app that overlays a subtle grain texture + soft haze on top of your
whole screen, mimicking the look of a matte (frosted) screen protector. It's
a click-through overlay, not a real-time blur, so it's cheap on battery and
needs no special permissions beyond "draw over other apps".

## How it works

- `NoiseOverlayView` procedurally generates a small tileable speckle texture
  once, then draws it (plus a soft white wash) across the full screen.
- `OverlayService` is a foreground service that adds this view as a
  `TYPE_APPLICATION_OVERLAY` window with `FLAG_NOT_TOUCHABLE`, so all touches
  pass straight through to whatever app is underneath.
- `MainActivity` lets you grant the overlay permission, toggle the filter,
  and adjust intensity with a slider (saved between launches).

## Building the APK from your phone (no laptop needed)

1. Create a new empty repo on GitHub (via the GitHub app or mobile site).
2. Upload every file/folder from this zip into that repo, **preserving the
   folder structure** (the `.github/workflows/build.yml` path especially
   must stay intact — GitHub's mobile web uploader supports drag-and-drop of
   the whole extracted folder, or use the GitHub app's "Add file" flow).
3. Once pushed to `main`, go to the repo's **Actions** tab. The "Build APK"
   workflow starts automatically.
4. When it finishes (a few minutes), open the workflow run and download the
   `matte-screen-filter-debug-apk` artifact — it's a zip containing
   `app-debug.apk`.
5. On your phone, allow installs from the source you're using (Files app /
   browser downloads) under Settings → Apps → Special access → Install
   unknown apps, then tap the APK to install.

You can also trigger a rebuild manually any time from the Actions tab via
"Run workflow" (workflow_dispatch).

## Notes

- This builds a **debug APK**, self-signed with Android's default debug key
  — perfectly fine for sideloading on your own device, not for the Play
  Store.
- `targetSdk` is set to 33 to avoid Android 14's stricter foreground-service
  type declarations, which keeps the manifest simple for personal use.
- If the first Action run fails, open the log — scaffolded Gradle projects
  occasionally need a version bump (e.g. AGP/Gradle compatibility) depending
  on what's preinstalled on the runner; the error message will point at the
  exact line.
