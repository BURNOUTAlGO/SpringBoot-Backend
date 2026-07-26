package in.abhinav.journalApp.controller;
import java.util.*;

import in.abhinav.journalApp.entity.JournalEntry;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    //NOTE : ON EVERYTIME YOU RUN THE APPLICATION THE OLD DATA INSIDE THE MAP WILL BE empty
    private Map<Long,JournalEntry> journalEntries = new HashMap<>();


    @GetMapping
    public List<JournalEntry> getAll(){
        return new ArrayList<>(journalEntries.values());
    }

    @PostMapping
    //@RequestBody tell the spring please take the data from request and turn into a java object which i want to store
    public boolean createEntry(@RequestBody  JournalEntry myEntry){
        journalEntries.put(myEntry.getId(),myEntry);
        return true;
    }
    @GetMapping("/id/{myId}")
    public JournalEntry getJournalEntryById(@PathVariable Long myId){
        return journalEntries.get(myId);
    }

    @DeleteMapping("/id/{myId}")
    public JournalEntry deleteJournalEntryById(@PathVariable Long myId){
        return journalEntries.remove(myId);
    }
    @PutMapping("/id/{id}")
    public JournalEntry updateJournalEntryById(@PathVariable Long id, @RequestBody JournalEntry myEntry){
        return journalEntries.put(id,myEntry);
    }





}
