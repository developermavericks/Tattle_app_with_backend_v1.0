package com.example.tattle.data

import com.example.tattle.models.Article
import com.example.tattle.models.Survey
import com.example.tattle.models.SurveyQuestion

object MockData {
    val articles = listOf(
        Article(
            id = "1",
            category = "Tech & AI",
            readTime = "30s read",
            headline = "Neon Exodus: City Grid 7 Faces Massive System Blackout",
            hook = "Technicians report a localized power failure in the cyber-district, affecting over 2 million residents. Grid authorities suspect a coordinated digital breach.",
            brief = "A massive power failure has plunged City Grid 7 into absolute darkness. Technicians are scrambling to isolate a rogue worm detected in the primary substation's firmware. Authorities are urging residents to rely on off-grid localized mesh cells for communication.",
            bullets = listOf(
                "2M+ residents are completely dark, with all transit and commerce lines offline.",
                "Cyber-security task forces have flagged a coordinated digital breach utilizing recycled hardware elements.",
                "Off-grid localized mesh networks are seeing record-high traffic as emergency lines choke."
            ),
            whyItMatters = "This isn't a simple infrastructure failure; it is a live-fire demonstration of modern grid warfare. If localized smart substation controls are vulnerable to recycled hardware exploits, every high-density metropolitan area is actively sitting on a ticking clock.",
            fullText = """The digital void isn't empty; it's overflowing. For the modern urban citizen, information is no longer a curated stream but a relentless flood. Yesterday's power failure in Sector 7 proves that our reliance on centralized digital systems has turned convenience into our greatest point of failure.

At precisely 22:14 local time, the core automated switches of City Grid 7 fell silent. According to lead firmware engineer Kaelen Vance, a polymorphic command loop bypassed the secondary firewalls by masquerading as a routine diagnostic packet. The attack was surgical, taking down localized distribution lines while leaving the primary generators spinning uselessly.

"What we are witnessing is the balkanization of infrastructure security," says cyber-defense analyst Alex Thorne. "When you mix modern automated smart grids with legacy components bought from secondary global markets, you create backdoor avenues that are almost impossible to audit in real-time."

For the next 6 hours, Grid 7 was forced into a state of absolute analog primitive existence. Local hackers quickly set up low-power FM relays and point-to-point Wi-Fi grids to coordinate basic logistics. This grassroots mesh response shows a resilient, decentralizing impulse among the younger demographic, who have zero trust in central utilities to keep them secure.

The incident is a stark reminder that as we accelerate into direct-to-brain interfaces and fully autonomous municipal agents, maintaining independent offline capacities is no longer just a hobbyist interest—it is a baseline survival protocol.""",
            publisher = "Tattle Reports",
            publishedAt = "2h ago",
            imageUrl = "https://picsum.photos/seed/tattle1/800/1000",
            likes = 1200,
            commentsCount = 482,
            isBreaking = true,
            views = 4500,
            language = "English"
        ),
        Article(
            id = "hi-1",
            category = "टेक और AI",
            readTime = "30 सेकंड का पाठ",
            headline = "नियॉन एक्सोडस: सिटी ग्रिड 7 में भारी सिस्टम ब्लैकआउट",
            hook = "तकनीशियनों ने साइबर-डिस्ट्रिक्ट में स्थानीय बिजली विफलता की सूचना दी है, जिससे 20 लाख से अधिक निवासी प्रभावित हुए हैं। ग्रिड अधिकारियों को एक समन्वित डिजिटल सेंधमारी का संदेह है।",
            brief = "एक भारी बिजली विफलता ने सिटी ग्रिड 7 को पूर्ण अंधेरे में धकेल दिया है। तकनीशियन प्राथमिक सबस्टेशन के फर्मवेयर में पाए गए एक दुष्ट वर्म को अलग करने की कोशिश कर रहे हैं। अधिकारी निवासियों से संचार के लिए ऑफ-ग्रिड स्थानीयकृत मेश सेल पर भरोसा करने का आग्रह कर रहे हैं।",
            bullets = listOf(
                "20 लाख से अधिक निवासी पूरी तरह अंधेरे में हैं, सभी पारगमन और वाणिज्य लाइनें ऑफलाइन हैं।",
                "साइबर-सुरक्षा टास्क फोर्स ने पुनर्नवीनीकरण हार्डवेयर तत्वों का उपयोग करके एक समन्वित डिजिटल सेंधमारी को चिह्नित किया है।",
                "आपातकालीन लाइनों के बंद होने के कारण ऑफ-ग्रिड स्थानीयकृत मेश नेटवर्क पर रिकॉर्ड-उच्च ट्रैफ़िक देखा जा रहा है।"
            ),
            whyItMatters = "यह केवल बुनियादी ढांचे की विफलता नहीं है; यह आधुनिक ग्रिड युद्ध का एक जीवंत प्रदर्शन है। यदि स्थानीयकृत स्मार्ट सबस्टेशन नियंत्रण पुनर्नवीनीकरण हार्डवेयर कारनामों के प्रति संवेदनशील हैं, तो प्रत्येक उच्च-घनत्व वाला महानगरीय क्षेत्र सक्रिय रूप से एक टिक-टिक करती घड़ी पर बैठा है।",
            fullText = "हिंदी में पूरा लेख यहाँ...",
            publisher = "टैटल रिपोर्ट्स",
            publishedAt = "2 घंटे पहले",
            imageUrl = "https://picsum.photos/seed/tattle_hi/800/1000",
            likes = 1100,
            commentsCount = 200,
            isBreaking = true,
            views = 3000,
            language = "Hindi"
        ),
        Article(
            id = "2",
            category = "Future Tech",
            readTime = "6 min read",
            headline = "The Silent Rise of Neural-Mesh Networks in Urban Grids",
            hook = "While telecom giants push expensive premium licenses, a grassroots community of hackers is weaving a completely free, sovereign internet using cheap radio nodes.",
            brief = "Decentralized mesh networks are silently spreading across major global metropolises, offering a secure alternative to corporate-monitored internet pathways.",
            bullets = listOf(
                "Solar nodes form a self-healing, peer-to-peer data web.",
                "Data is split across multiple pathways, making surveillance impossible.",
                "Over 45,000 active devices are online across Tokyo and Berlin."
            ),
            whyItMatters = "Neural-mesh networks represent a technological reclamation of sovereignty.",
            fullText = """While the world's largest telecommunications conglomerates argue...""",
            publisher = "Tattle Reports",
            publishedAt = "3h ago",
            imageUrl = "https://picsum.photos/seed/tattle2/800/1000",
            likes = 1540,
            commentsCount = 312,
            isBreaking = false,
            views = 3200,
            language = "English"
        ),
        Article(
            id = "hi-2",
            category = "भविष्य की तकनीक",
            readTime = "6 मिनट का पाठ",
            headline = "शहरी ग्रिडों में न्यूरल-मेश नेटवर्क का मौन उदय",
            hook = "जबकि टेलीकॉम दिग्गज महंगे प्रीमियम लाइसेंस बेच रहे हैं, हैकर्स का एक जमीनी समुदाय सस्ते रेडियो नोड्स का उपयोग करके पूरी तरह से मुफ्त, संप्रभु इंटरनेट बुन रहा है।",
            brief = "विकेंद्रीकृत मेश नेटवर्क प्रमुख वैश्विक महानगरों में चुपचाप फैल रहे हैं, जो कॉर्पोरेट-निगरानी वाले इंटरनेट मार्गों का एक सुरक्षित विकल्प प्रदान करते हैं।",
            bullets = listOf(
                "सौर नोड्स एक स्व-चिकित्सा, पीयर-टू-पीयर डेटा वेब बनाते हैं।",
                "डेटा कई मार्गों में विभाजित है, जिससे निगरानी असंभव हो जाती है।",
                "टोक्यो और बर्लिन में 45,000 से अधिक सक्रिय डिवाइस ऑनलाइन हैं।"
            ),
            whyItMatters = "न्यूरल-मेश नेटवर्क संप्रभुता के तकनीकी सुधार का प्रतिनिधित्व करते हैं।",
            fullText = "हिंदी में पूरा लेख यहाँ...",
            publisher = "टैटल रिपोर्ट्स",
            publishedAt = "3 घंटे पहले",
            imageUrl = "https://picsum.photos/seed/tattle_hi2/800/1000",
            likes = 1400,
            commentsCount = 250,
            isBreaking = false,
            views = 2800,
            language = "Hindi"
        ),
        Article(
            id = "3",
            category = "Pop Culture",
            readTime = "4 min read",
            headline = "Virtual Collective: The underground scene of digital nomads",
            hook = "Bypassing conventional residency laws, a new nomadic generation is establishing virtual micronations.",
            brief = "A new wave of decentralized digital nomads are abandoning geographical citizenship.",
            bullets = listOf(
                "Members pool income into shared smart contracts.",
                "Collective members share living spaces and travel dynamically.",
                "Highly skilled digital labor relocates out of reach of traditional states."
            ),
            whyItMatters = "Gen Z is actively inventing a post-national way of living.",
            fullText = """The traditional concept of a 'nation' is built on dirt...""",
            publisher = "Creator Lab",
            publishedAt = "5h ago",
            imageUrl = "https://picsum.photos/seed/tattle3/800/1000",
            likes = 980,
            commentsCount = 198,
            isBreaking = false,
            views = 2100,
            language = "English"
        ),
        Article(
            id = "4",
            category = "Politics",
            readTime = "30s read",
            headline = "Decentralized Voting: Trial runs show surprising results",
            hook = "A modern district-level voting trial using ledger-verified tokens recorded over 94% participation among youth.",
            brief = "A pilot program for localized cryptographically secure voting has successfully concluded.",
            bullets = listOf(
                "Youth voter participation spiked to 94.2%.",
                "Results were compiled and verified within 4 minutes.",
                "Public support is massive despite traditional party questions."
            ),
            whyItMatters = "When you make voting fast and secure, the political landscape shifts instantaneously.",
            fullText = """For decades, political scientists have repeated...""",
            publisher = "CivicTech Monthly",
            publishedAt = "8h ago",
            imageUrl = "https://picsum.photos/seed/tattle4/800/1000",
            likes = 2120,
            commentsCount = 654,
            isBreaking = false,
            views = 6700,
            language = "English"
        ),
        Article(
            id = "5",
            category = "Tech & AI",
            readTime = "5 min read",
            headline = "Global Semiconductor Shortage Hits Critical Infrastructure",
            hook = "The semiconductor supply chain crisis has escalated, impacting metropolitan energy grids.",
            brief = "High-density urban energy grids are experiencing implementation lags due to microcontroller delays.",
            bullets = listOf(
                "Implementations are stalled until Q3 2025.",
                "Secondary market pricing for legacy controllers spiked 400%.",
                "Recycled chips increase the attack surface for hardware exploits."
            ),
            whyItMatters = "When fundamental infrastructure can't access hardware, societal stability becomes a variable.",
            fullText = """The ongoing semiconductor supply chain crisis...""",
            publisher = "Tattle Reports",
            publishedAt = "12h ago",
            imageUrl = "https://picsum.photos/seed/tattle5/800/1000",
            likes = 3410,
            commentsCount = 912,
            isBreaking = true,
            views = 9400,
            language = "English"
        ),
        Article(
            id = "6",
            category = "Tech & AI",
            readTime = "4 min read",
            headline = "Neural Linkage: The Ethics of Direct-to-Brain Computing",
            hook = "As consumer neural implants pass clinical trials, how do you keep thoughts private?",
            brief = "Neural connection implants are transitioning from labs to mainstream pre-orders.",
            bullets = listOf(
                "Pre-orders for reading-headbands exceeded 500,000 units.",
                "No direct laws exist to prevent aggregating focus metrics.",
                "Hacker groups are designing local hardware-kill switches."
            ),
            whyItMatters = "We must establish neural-rights frameworks before the tech becomes default.",
            fullText = """The ultimate frontier isn't space...""",
            publisher = "NeuroTech Daily",
            publishedAt = "1d ago",
            imageUrl = "https://picsum.photos/seed/tattle6/800/1000",
            likes = 870,
            commentsCount = 142,
            isBreaking = false,
            views = 1800,
            language = "English"
        )
    )

    val surveys = listOf(
        Survey(
            id = "s1",
            title = "Tattle Checkpoint",
            questions = listOf(
                SurveyQuestion(
                    id = "q1",
                    question = "Which trend are you most excited about for the next 5 years?",
                    options = listOf(
                        "Decentralized Off-Grid Internet (Mesh)",
                        "Cognitive Liberty & Mind Privacy",
                        "Direct-to-Device Cryptographic Voting",
                        "Virtual Micronations & Cloud Cooperatives"
                    )
                )
            )
        )
    )
}
