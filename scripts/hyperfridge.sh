#!/bin/bash

readonly_dir="${IN_DIR:-./trace}"
work_dir="${WORK_DIR:-./hf}"
processing_dir="${work_dir}/work"
done_dir="${work_dir}/done"
error_dir="${work_dir}/error"

# Create processing, done, and error directories if they don't exist
mkdir -p "$work_dir"
mkdir -p "$processing_dir"
mkdir -p "$done_dir"
mkdir -p "$error_dir"

# Function to process a file
process_file() {
    local filename="$1"
    
    # Copy file to processing directory
    cp "$readonly_dir/$filename" "$processing_dir/$filename"

    # Call external script to process the file
    ./external_script.sh "$processing_dir/$filename"
    local exit_code=$?
    
    # Check exit code for errors
    if [ $exit_code -eq 0 ]; then
        # Move file to done directory
        mv "$processing_dir/$filename" "$done_dir/$filename"
        echo "File $filename processed successfully."
    else
        # Move file to error directory
        mv "$processing_dir/$filename" "$error_dir/$filename"
        echo "Error processing file $filename."
    fi
}

# Monitor readonly directory for file creation events
inotifywait -m -e create --format '%f' "$readonly_dir" | while read -r filename; do
    process_file "$filename"
done
