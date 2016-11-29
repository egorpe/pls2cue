tell application "iTunes"
	copy view of front window to selectedLibrary
	copy id of selectedLibrary to playlistId
	do shell script "~/bin/make_cue_sheet " & playlistId
	display alert "Done!"
end tell
