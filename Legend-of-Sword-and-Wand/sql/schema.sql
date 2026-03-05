-- 1. USERS TABLE
-- Handles profile creation and authentication as required by the use cases[cite: 171, 172].
CREATE TABLE Users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL, -- In a real app, store hashed passwords
                       pvp_wins INT DEFAULT 0,         -- To track league stats for PvP [cite: 314]
                       pvp_losses INT DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. PARTIES TABLE
-- Stores saved parties and campaign progress.
-- A user can have up to 5 saved parties[cite: 185, 295].
CREATE TABLE Parties (
                         party_id INT AUTO_INCREMENT PRIMARY KEY,
                         user_id INT NOT NULL,
                         party_name VARCHAR(50),
                         is_active_campaign BOOLEAN DEFAULT TRUE, -- Distinguishes between a current run and a saved PvP team
                         current_room INT DEFAULT 1,              -- Tracks progress up to room 30 [cite: 182]
                         gold INT DEFAULT 0,                      -- Stores gold for purchasing items [cite: 293]
                         score INT DEFAULT 0,                     -- Calculated score at the end of a campaign [cite: 182]
                         FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 3. HEROES TABLE
-- Stores individual hero stats. A party has 1 to 5 members[cite: 257].
CREATE TABLE Heroes (
                        hero_id INT AUTO_INCREMENT PRIMARY KEY,
                        party_id INT NOT NULL,
                        hero_name VARCHAR(50),
                        hero_class VARCHAR(20),       -- Order, Chaos, Warrior, Mage [cite: 216]
                        level INT DEFAULT 1,          -- Heroes start at level 1 and go up to 20 [cite: 252, 255]
                        experience INT DEFAULT 0,     -- Experience points [cite: 276]
                        hp_current INT DEFAULT 100,   -- Default starting HP [cite: 253]
                        hp_max INT DEFAULT 100,
                        mana_current INT DEFAULT 50,  -- Default starting Mana [cite: 253]
                        mana_max INT DEFAULT 50,
                        attack INT DEFAULT 5,         -- Default starting Attack [cite: 253]
                        defense INT DEFAULT 5,        -- Default starting Defense [cite: 253]
                        FOREIGN KEY (party_id) REFERENCES Parties(party_id) ON DELETE CASCADE
);

-- 4. INVENTORY TABLE
-- Stores items owned by the party (e.g., Bread, Elixir)[cite: 299].
CREATE TABLE Inventory (
                           inventory_id INT AUTO_INCREMENT PRIMARY KEY,
                           party_id INT NOT NULL,
                           item_name VARCHAR(50),        -- 'Bread', 'Cheese', 'Steak', 'Water', 'Juice', 'Wine', 'Elixir'
                           quantity INT DEFAULT 0,
                           FOREIGN KEY (party_id) REFERENCES Parties(party_id) ON DELETE CASCADE
);

-- 5. HIGH SCORES TABLE (HALL OF FAME)
-- Keeps a record of the highest scores among players[cite: 296].
CREATE TABLE HighScores (
                            score_id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            score INT NOT NULL,
                            date_achieved DATE,
                            FOREIGN KEY (user_id) REFERENCES Users(user_id)
);